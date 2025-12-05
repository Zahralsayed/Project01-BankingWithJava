package com.bank.accounts;

import java.time.LocalDateTime;

public class CheckingAccount extends Account {
    public CheckingAccount() {}
    public CheckingAccount(String accountId, String customerId, double initialBalance, CardType card) {
        super(accountId, customerId, initialBalance, card, AccountType.Checking);
    }
    LocalDateTime now = LocalDateTime.now();

    @Override
    public void deposit(double amount, String description) throws Exception{
        Transaction t = new Transaction(accountId, Transaction.TransactionType.Deposit, amount, balance + amount, now, description);
        addTransaction(t);
    }

    @Override
    public void withdraw(double amount, String description) throws Exception{
        if (amount > balance){
            incrementOverdraftCount();
            throw new Exception("Insufficient Funds in Checking Account");
        }

        Transaction t = new Transaction(accountId, Transaction.TransactionType.Withdraw, amount, balance - amount, now, description);
        addTransaction(t);
    }
}
