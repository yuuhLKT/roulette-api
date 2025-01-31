package com.example.roulette.controller;

import com.example.roulette.dto.*;
import com.example.roulette.exception.UserNotFoundException;
import com.example.roulette.model.User;
import com.example.roulette.service.RouletteService;
import com.example.roulette.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/roulette")
@Tag(name = "Roulette API", description = "API for managing roulette game")
public class RouletteController {

    private final RouletteService rouletteService;
    private final UserService userService;

    public RouletteController(RouletteService rouletteService, UserService userService) {
        this.rouletteService = rouletteService;
        this.userService = userService;
    }

    @PostMapping("/bet")
    @Operation(summary = "Place a bet", description = "Places a bet in the current round")
    public ResponseEntity<BetResponseDTO> placeBet(@RequestBody BetRequestDTO betRequest) {
        try {
            BetResponseDTO betResponse = rouletteService.placeBet(betRequest);
            return ResponseEntity.ok(betResponse);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @PostMapping("/user")
    @Operation(summary = "Create a new user", description = "Creates a new user with the given username and initial balance")
    public ResponseEntity<User> createUser(@RequestBody UserRequestDTO userRequest) {
        User user = userService.createUser(userRequest.getUsername(), userRequest.getInitialBalance());
        return ResponseEntity.ok(user);
    }

    @PostMapping("/user/{userId}/balance")
    @Operation(summary = "Add balance to a user", description = "Adds the specified amount to the user's balance")
    public ResponseEntity<User> addBalance(
            @PathVariable @Parameter(description = "ID of the user") Long userId,
            @RequestBody BalanceRequestDTO balanceRequest) {
        try {
            User user = userService.addBalance(userId, balanceRequest.getAmount());
            return ResponseEntity.ok(user);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping("/users")
    @Operation(summary = "Get all users", description = "Retrieves a list of all users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user by ID", description = "Retrieves a user by their ID")
    public ResponseEntity<User> getUserById(
            @PathVariable @Parameter(description = "ID of the user") Long userId) {
        try {
            User user = userService.getUserById(userId);
            return ResponseEntity.ok(user);
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping("/round/{roundId}")
    @Operation(summary = "Get round status by ID", description = "Retrieves the status of a round by its ID")
    public ResponseEntity<RoundResponseDTO> getRoundById(
            @PathVariable @Parameter(description = "ID of the round") Long roundId) {
        RoundResponseDTO roundResponse = rouletteService.getRoundById(roundId);
        return ResponseEntity.ok(roundResponse);
    }

    @GetMapping("/rounds")
    @Operation(summary = "Get all rounds", description = "Retrieves a list of all rounds and their data")
    public ResponseEntity<List<RoundResponseDTO>> getAllRounds() {
        List<RoundResponseDTO> rounds = rouletteService.getAllRounds();
        return ResponseEntity.ok(rounds);
    }

    @GetMapping("/user/{userId}/bets")
    @Operation(summary = "Get bets by user ID", description = "Retrieves the bets of a user by their ID")
    public ResponseEntity<List<BetResponseDTO>> getBetsByUserId(
            @PathVariable @Parameter(description = "ID of the user") Long userId) {
        List<BetResponseDTO> bets = rouletteService.getBetsByUserId(userId);
        return ResponseEntity.ok(bets);
    }

    @DeleteMapping("/user/{userId}")
    @Operation(summary = "Delete a user", description = "Deletes a user by their ID")
    public ResponseEntity<Void> deleteUser(
            @PathVariable @Parameter(description = "ID of the user") Long userId) {
        try {
            userService.deleteUser(userId);
            return ResponseEntity.noContent().build();
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}