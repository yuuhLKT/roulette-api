package com.example.roulette.service;

import com.example.roulette.dto.BetRequestDTO;
import com.example.roulette.dto.BetResponseDTO;
import com.example.roulette.dto.RoundResponseDTO;
import com.example.roulette.exception.UserNotFoundException;
import com.example.roulette.model.*;
import com.example.roulette.repository.BetRepository;
import com.example.roulette.repository.RoundRepository;
import com.example.roulette.repository.UserRepository;
import com.example.roulette.websocket.WebSocketHandler;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class RouletteService {

    private final BetRepository betRepository;
    private final RoundRepository roundRepository;
    private final UserRepository userRepository;
    private final WebSocketHandler webSocketHandler;
    private final Random random;
    private final ScheduledExecutorService scheduler;
    private Round currentRound;

    public RouletteService(BetRepository betRepository, RoundRepository roundRepository, UserRepository userRepository, WebSocketHandler webSocketHandler) {
        this.betRepository = betRepository;
        this.roundRepository = roundRepository;
        this.userRepository = userRepository;
        this.webSocketHandler = webSocketHandler;
        this.random = new Random();
        this.scheduler = Executors.newScheduledThreadPool(1);
    }

    private void startRound() {
        currentRound = new Round();
        currentRound.setStatus(RoundStatus.WAITING);
        currentRound.setStartTime(LocalDateTime.now());
        currentRound = roundRepository.save(currentRound);

        scheduler.schedule(this::startInProgress, 30, TimeUnit.SECONDS);

        scheduler.scheduleAtFixedRate(() -> {
            long elapsedTime = (System.currentTimeMillis() - currentRound.getStartTime().toInstant(ZoneOffset.UTC).toEpochMilli()) / 1000;
            long remainingTime = 30 - elapsedTime;
            if (remainingTime <= 0) {
                return;
            }

            RoundResponseDTO roundResponse = createRoundResponse(currentRound);
            roundResponse.setTimeRemaining(remainingTime);
            webSocketHandler.notifyClients(roundResponse);

        }, 0, 10, TimeUnit.SECONDS);
    }

    private void startInProgress() {
        if (currentRound == null) {
            return;
        }

        currentRound.setStatus(RoundStatus.IN_PROGRESS);
        roundRepository.save(currentRound);

        RoundResponseDTO roundResponse = createRoundResponse(currentRound);
        webSocketHandler.notifyClients(roundResponse);

        scheduler.schedule(this::finishRound, 5, TimeUnit.SECONDS);
    }

    public BetResponseDTO placeBet(BetRequestDTO betRequest) {
        if (currentRound == null || currentRound.getStatus() != RoundStatus.WAITING) {
            startRound();
        }

        User user = userRepository.findById(betRequest.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (user.getBalance().compareTo(betRequest.getAmount()) < 0) {
            throw new IllegalArgumentException("Insufficient balance");
        }

        user.setBalance(user.getBalance().subtract(betRequest.getAmount()));
        userRepository.save(user);

        Bet bet = new Bet();
        bet.setAmount(betRequest.getAmount());
        bet.setColor(betRequest.getColor());
        bet.setRound(currentRound);
        bet.setStatus(BetStatus.PENDING);
        bet.setUser(user);
        bet.setTimestamp(LocalDateTime.now());
        bet = betRepository.save(bet);

        BetResponseDTO betResponse = new BetResponseDTO();
        betResponse.setId(bet.getId());
        betResponse.setUserId(bet.getUser().getId());
        betResponse.setAmount(bet.getAmount());
        betResponse.setColor(bet.getColor());
        betResponse.setStatus(bet.getStatus());
        betResponse.setTimestamp(bet.getTimestamp());

        RoundResponseDTO roundResponse = createRoundResponse(currentRound);
        roundResponse.setBets(processBets(betRepository.findByRound(currentRound)));
        webSocketHandler.notifyClients(roundResponse);

        return betResponse;
    }

    private void finishRound() {
        if (currentRound == null) {
            return;
        }

        currentRound.setStatus(RoundStatus.FINISHED);
        currentRound.setWinningColor(spinRoulette());
        roundRepository.save(currentRound);

        List<Bet> bets = betRepository.findByRound(currentRound);
        calculateWinnings(bets, currentRound.getWinningColor());

        List<BetResponseDTO> betResponses = processBets(bets);

        RoundResponseDTO roundResponse = createRoundResponse(currentRound);
        roundResponse.setBets(betResponses);

        webSocketHandler.notifyClients(roundResponse);

        currentRound = null;
    }

    private List<BetResponseDTO> processBets(List<Bet> bets) {
        return bets.stream().map(bet -> {
            BetResponseDTO betResponse = new BetResponseDTO();
            betResponse.setId(bet.getId());
            betResponse.setUserId(bet.getUser().getId());
            betResponse.setAmount(bet.getAmount());
            betResponse.setColor(bet.getColor());
            betResponse.setStatus(bet.getStatus());
            betResponse.setTimestamp(bet.getTimestamp());
            betResponse.setRoundId(bet.getRound().getId());
            betResponse.setWinnings(bet.getWinnings());
            return betResponse;
        }).toList();
    }

    private Color spinRoulette() {
        List<Color> colors = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            colors.add(Color.RED);
            colors.add(Color.BLACK);
        }
        colors.add(Color.GREEN);
        return colors.get(random.nextInt(colors.size()));
    }

    private void calculateWinnings(List<Bet> bets, Color winningColor) {
        for (Bet bet : bets) {
            User user = bet.getUser();
            BigDecimal winnings = BigDecimal.ZERO;

            if (bet.getColor() == winningColor) {
                bet.setStatus(BetStatus.WON);
                if (winningColor == Color.GREEN) {
                    winnings = bet.getAmount().multiply(BigDecimal.valueOf(14));
                } else {
                    winnings = bet.getAmount().multiply(BigDecimal.valueOf(2));
                }
            } else {
                bet.setStatus(BetStatus.LOST);
                winnings = bet.getAmount().negate();
            }

            bet.setWinnings(winnings);
            user.setBalance(user.getBalance().add(winnings));
            userRepository.save(user);
            betRepository.save(bet);
        }
    }

    private RoundResponseDTO createRoundResponse(Round round) {
        RoundResponseDTO roundResponse = new RoundResponseDTO();
        roundResponse.setId(round.getId());
        roundResponse.setWinningColor(round.getWinningColor());
        roundResponse.setStatus(round.getStatus());
        roundResponse.setBets(new ArrayList<>());
        return roundResponse;
    }

    public RoundResponseDTO getRoundById(Long roundId) {
        Round round = roundRepository.findById(roundId)
                .orElseThrow(() -> new IllegalArgumentException("Round not found"));
        return createRoundResponse(round);
    }

    public List<RoundResponseDTO> getAllRounds() {
        List<Round> rounds = roundRepository.findAll();
        return rounds.stream().map(this::createRoundResponse).toList();
    }

    public List<BetResponseDTO> getBetsByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        List<Bet> bets = betRepository.findByUser(user);

        return bets.stream().map(bet -> {
            BetResponseDTO betResponse = new BetResponseDTO();
            betResponse.setId(bet.getId());
            betResponse.setUserId(bet.getUser().getId());
            betResponse.setAmount(bet.getAmount());
            betResponse.setColor(bet.getColor());
            betResponse.setStatus(bet.getStatus());
            betResponse.setTimestamp(bet.getTimestamp());
            betResponse.setRoundId(bet.getRound().getId());
            betResponse.setWinnings(bet.getWinnings());
            return betResponse;
        }).toList();
    }
}