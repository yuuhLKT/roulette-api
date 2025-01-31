package com.example.roulette.repository;

import com.example.roulette.model.Bet;
import com.example.roulette.model.Round;
import com.example.roulette.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BetRepository extends JpaRepository<Bet, Long> {
    List<Bet> findByRound(Round currentRound);

    List<Bet> findByUser(User user);
}