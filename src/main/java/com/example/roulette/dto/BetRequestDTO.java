package com.example.roulette.dto;

import com.example.roulette.model.Color;
import java.math.BigDecimal;

public class BetRequestDTO {
    private Long userId;
    private BigDecimal amount;
    private Color color;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }
}