package com.example.roulette.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "rounds")
public class Round {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "winning_color")
    private Color winningColor;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoundStatus status;

    @OneToMany(mappedBy = "round")
    private List<Bet> bets;

    public Round() {
    }

    public Round(Long id, Color winningColor, LocalDateTime startTime, LocalDateTime endTime, RoundStatus status, List<Bet> bets) {
        this.id = id;
        this.winningColor = winningColor;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.bets = bets;
    }

    @PrePersist
    protected void onCreate() {
        startTime = LocalDateTime.now();
        status = RoundStatus.WAITING;
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

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public RoundStatus getStatus() {
        return status;
    }

    public void setStatus(RoundStatus status) {
        this.status = status;
    }

    public List<Bet> getBets() {
        return bets;
    }

    public void setBets(List<Bet> bets) {
        this.bets = bets;
    }
}