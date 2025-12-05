package com.bank.accounts;

import java.util.ArrayList;
import java.util.List;

public abstract class Account implements Transactional{
    protected String accountId;
    protected String customerId;
    protected double balance;
    protected CardType card;
    private boolean active = true;
    private int overdraftCount= 0;
    private List<Transaction> transactionHistory = new ArrayList<>();

    protected AccountType accountType;

    public enum AccountType{
        Checking,
        Saving
    }

    public Account(){}

    public Account(String accountId, String customerId, double balance, CardType card, AccountType accountType) {
        this.accountId = accountId;
        this.customerId = customerId;
        this.balance = balance;
        this.card = card;
        this.accountType = accountType;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public double getBalance() {
        return balance;
    }

    public CardType getCard() {
        return card;
    }

    public boolean isActive() {
        return active;
    }

    public int getOverdraftCount() {
        return overdraftCount;
    }

    public void incrementOverdraftCount(){
        overdraftCount++;
    }

    public void resetOverdraftCount(){
        overdraftCount = 0;
    }

    public List<Transaction> getTransactionHistory() {
        return transactionHistory;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public void deactivate() {
        active = false;
    }

    public void activate() {
        active = true;
    }

    private static int savingCounter=1;
    private static int checkingCounter=1;
    public static String generateAccountNumber(AccountType type) {
        String prefix = (type == AccountType.Saving) ? "S-" : "C-";
        int number = (type == AccountType.Saving) ? savingCounter++ : checkingCounter++;

        return prefix + String.format("%02d", number);
    }

    public void addTransaction(Transaction t) {
        if (t.getAccountId().equals(this.accountId)) {
            transactionHistory.add(t);
            balance = t.getPostBalance();
        }
    }

}
