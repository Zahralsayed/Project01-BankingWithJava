package com.bank.accounts;

import java.time.LocalDateTime;

public class Transaction {
    protected String accountId;
    protected TransactionType type;
    protected double amount;
    protected double postBalance;
    protected LocalDateTime dateTime;
    protected String description;

    public enum TransactionType{
        Deposit,
        Withdraw,
        Transfer_In,
        Transfer_Out,
        Overdraft_fee
    }

    public Transaction(){}

    public Transaction(String accountId, TransactionType type, double amount, double postBalance, LocalDateTime dateTime, String description) {
        this.accountId = accountId;
        this.type = type;
        this.amount = amount;
        this.postBalance = postBalance;
        this.dateTime = LocalDateTime.now();
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
