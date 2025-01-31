package com.example.roulette.dto;

import com.example.roulette.model.Color;
import com.example.roulette.model.RoundStatus;
import java.util.List;

public class RoundResponseDTO {
    private Long id;
    private Color winningColor;
    private RoundStatus status;
    private List<BetResponseDTO> bets;
    private long timeRemaining;

    public long getTimeRemaining() {
        return timeRemaining;
    }

    public void setTimeRemaining(long timeRemaining) {
        this.timeRemaining = timeRemaining;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Color getWinningColor() {
        return winningColor;
    }

    public void setWinningColor(Color winningColor) {
        this.winningColor = winningColor;
    }

    public RoundStatus getStatus() {
        return status;
    }

    public void setStatus(RoundStatus status) {
        this.status = status;
    }

    public List<BetResponseDTO> getBets() {
        return bets;
    }

    public void setBets(List<BetResponseDTO> bets) {
        this.bets = bets;
    }
}