package com.bank.accounts;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public abstract class Account implements Transactional {
    protected String accountId;
    protected String customerId;
    protected double balance;
    protected CardType card;
    private boolean active = true;
    private int overdraftCount = 0;
    private double overdraftFees = 0.0;
    private List<Transaction> transactionHistory = new ArrayList<>();

    protected AccountType accountType;
    protected double withdrawnToday = 0;
    protected double depositedToday = 0;
    protected LocalDate lastResetDate = LocalDate.now();

    public enum AccountType {
        Checking,
        Saving
    }

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

    public void setCard(CardType newCardType) {
        card = newCardType;
    }

    public boolean isActive() {
        return active;
    }

    public int getOverdraftCount() {
        return overdraftCount;
    }

    public void incrementOverdraftCount() {
        overdraftCount++;
    }

    public void resetOverdraftCount() {
        overdraftCount = 0;
    }

    public void setOverdraftCount(int overdraftCount) {
        this.overdraftCount = overdraftCount;
    }

    public double getOverdraftFees() {
        return overdraftFees;
    }

    public void addOverdraftFee(double fee) {
        overdraftFees += fee;
    }

    public void resetOverdraftFees() {
        overdraftFees = 0.0;
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

    public static String generateAccountNumber(AccountType type) {
        String prefix = (type == AccountType.Saving) ? "ACC-S-" : "ACC-C-";
        ThreadLocalRandom rand = ThreadLocalRandom.current();
        StringBuilder accNum = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            accNum.append(rand.nextInt(10));
        }

        return prefix + accNum;
    }

    public void addTransaction(Transaction t) {

        boolean exists = transactionHistory.stream().anyMatch(existing ->
                existing.getAccountId().equals(t.getAccountId())
                        && existing.getDateTime().equals(t.getDateTime())
                        && existing.getType() == t.getType()
                        && existing.getAmount() == t.getAmount()
        );

        if (!exists) {
            if (t.getAccountId().equals(this.accountId)) {
                transactionHistory.add(t);
                balance = t.getPostBalance();
            }
        }
    }

    protected void resetIfNewDay() {
        if (!lastResetDate.equals(LocalDate.now())) {
            withdrawnToday = 0;
            depositedToday = 0;
            lastResetDate = LocalDate.now();
        }
    }

    public boolean canWithdrawAmount(double amount) {
        resetIfNewDay();
        double limit = card.dailyWithdrawLimit();
        System.out.println("Daily Withdraw Limit:" + limit);
        LocalDate today = LocalDate.now();

        double withdrawnToday = transactionHistory.stream()
                .filter(tx -> tx.getType() == Transaction.TransactionType.Withdraw )
                .filter(tx -> tx.getDateTime().toLocalDate().isEqual(today))
                .mapToDouble(Transaction::getAmount)
                .sum();

        System.out.println("Trying to Withdraw: " +amount);

        if (withdrawnToday + amount > limit) {
            System.out.println("Daily withdraw limit reached! Max: " + limit +", Current: " + withdrawnToday);
            return false;
        }
        return true;
    }

    public boolean canDepositAmount(double amount, boolean ownAccount) {
        resetIfNewDay();

        double limit = ownAccount ? card.dailyDepositLimit() : card.dailyDepositLimit();
        System.out.println("Daily Limit: " + limit);

        LocalDate today = LocalDate.now();

        double depositedToday = transactionHistory.stream()
                .filter(tx -> tx.getType() == Transaction.TransactionType.Deposit)
                .filter(tx -> tx.getDateTime().toLocalDate().isEqual(today))
                .mapToDouble(Transaction::getAmount)
                .sum();

//        System.out.println("Deposited Today: " + depositedToday);
        System.out.println("Trying to Deposit: " + amount);

        if (depositedToday + amount > limit) {
            System.out.println("Daily deposit limit reached! " +
                    "Max: " + limit + ", Current: " + depositedToday);
            return false;
        }

        return true;
    }


    public boolean canTransferAmount(double amount, boolean ownAccount) {
        resetIfNewDay();
        double limit = ownAccount ? card.dailyOwnTransferLimit() : card.dailyTransferLimit();
        System.out.println("Daily Transfer Limit: " + limit);

        LocalDate today = LocalDate.now();

        double transferredToday = transactionHistory.stream()
                .filter(this::isTransferTransaction)
                .filter(tx -> tx.getDateTime().toLocalDate().isEqual(today))
                .mapToDouble(Transaction::getAmount)
                .sum();
        System.out.println("Attempting Transfer: " + amount);

            if (transferredToday + amount > limit) {
                String msg = ownAccount ?
                "Daily own-account transfer limit reached!" :
                        "Daily transfer-to-others limit reached!" ;

                System.out.println(msg +" Max: " + limit);
                return false;
            }
        return true;
    }

    private boolean isTransferTransaction(Transaction tx) {
        return tx.getType() == Transaction.TransactionType.Withdraw &&
                tx.getDescription().toLowerCase().contains("transferred from");
    }

}
