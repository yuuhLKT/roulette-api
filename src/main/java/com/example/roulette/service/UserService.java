package com.example.roulette.service;

import com.example.roulette.exception.UserNotFoundException;
import com.example.roulette.model.Bet;
import com.example.roulette.model.User;
import com.example.roulette.repository.BetRepository;
import com.example.roulette.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BetRepository betRepository;

    public UserService(UserRepository userRepository, BetRepository betRepository) {
        this.userRepository = userRepository;
        this.betRepository = betRepository;
    }

    public User createUser(String username, BigDecimal initialBalance) {
        User user = new User();
        user.setUsername(username);
        user.setBalance(initialBalance);
        return userRepository.save(user);
    }

    public User addBalance(Long userId, BigDecimal amount) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        user.setBalance(user.getBalance().add(amount));
        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        List<Bet> bets = betRepository.findByUser(user);
        betRepository.deleteAll(bets);

        userRepository.delete(user);
    }
}