package com.bank.accounts;

import java.time.LocalDateTime;

public class SavingAccount extends Account {
    public SavingAccount(String accountId, String customerId, double initialBalance, CardType card) {
        super(accountId, customerId, initialBalance, card, AccountType.Saving);
    }

    double postBalance = 0.0;

    @Override
    public void deposit(double amount, String description) throws Exception {

        postBalance = balance + amount;
        Transaction t = new Transaction(accountId, Transaction.TransactionType.Deposit, amount, balance, postBalance, LocalDateTime.now(), description);
        addTransaction(t);

        balance = postBalance;

        if (isActive()) return;
        if (postBalance < getOverdraftFees()) return;

        double feeAmount = getOverdraftFees();
        double postAfterFee = postBalance - feeAmount;

        postBalance -= getOverdraftFees();
        Transaction feeTx = new Transaction(accountId, Transaction.TransactionType.Fee, feeAmount, postBalance, postAfterFee, LocalDateTime.now(), "Overdraft Fee Deduction");
        addTransaction(feeTx);

        activate();
        resetOverdraftCount();
        resetOverdraftFees();

        balance = postAfterFee;
    }

    @Override
    public void withdraw(double amount, String description) throws Exception {
        if (!isActive()) throw new Exception("Account is not active");

        postBalance = balance - amount;

        if (postBalance >= 0) {
            Transaction t = new Transaction(accountId, Transaction.TransactionType.Withdraw, amount, balance, postBalance, LocalDateTime.now(), description);
            addTransaction(t);
            return;
        }

        if (postBalance < -100) {
            throw new Exception("Insufficient funds: limit $100 overdraft exceeded");
        }
        postBalance -= 35;
        addOverdraftFee(35);
        incrementOverdraftCount();

        if (getOverdraftCount() >= 2) deactivate();

        Transaction t = new Transaction(accountId, Transaction.TransactionType.Overdraft, amount, balance, postBalance, LocalDateTime.now(), description + " (Overdraft + $35 fee)");
        addTransaction(t);
    }
}