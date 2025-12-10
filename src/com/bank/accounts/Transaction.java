package com.bank.accounts;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Transaction {
    protected String accountId;
    protected TransactionType type;
    protected double amount;
    protected double preBalance;
    protected double postBalance;
    protected LocalDateTime dateTime;
    protected String description;

    public enum TransactionType{
        Deposit,
        Withdraw,
        Overdraft,
        Fee
    }

    public Transaction(String accountId, TransactionType type, double amount, double preBalance, double postBalance, LocalDateTime dateTime, String description) {
        this.accountId = accountId;
        this.type = type;
        this.amount = amount;
        this.preBalance = preBalance;
        this.postBalance = postBalance;
        this.dateTime = dateTime;
        this.description = description;
    }

    public String getAccountId() {
        return accountId;
    }

    public TransactionType getType() {
        return type;
    }

    public double getAmount() {
        return amount;
    }

    public double getPreBalance() {
        return preBalance;
    }

    public double getPostBalance() {
        return postBalance;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return  dateTime + " | " + accountId + " | " + type + " | Amount: $" + amount + " | Balance: $" + postBalance + " | Description: " + description;
    }
}
