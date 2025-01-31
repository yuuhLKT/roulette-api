package com.example.roulette.dto;

import java.math.BigDecimal;

public class UserRequestDTO {
    private String username;
    private BigDecimal initialBalance;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public BigDecimal getInitialBalance() {
        return initialBalance;
    }

    public void setInitialBalance(BigDecimal initialBalance) {
        this.initialBalance = initialBalance;
    }
}