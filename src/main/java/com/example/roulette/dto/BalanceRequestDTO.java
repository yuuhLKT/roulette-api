package com.example.roulette.dto;

import java.math.BigDecimal;

public class BalanceRequestDTO {
    private BigDecimal amount;

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}