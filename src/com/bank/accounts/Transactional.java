package com.bank.accounts;

public interface Transactional {
    void deposit(double amount, String description) throws Exception;
    void withdraw(double amount, String description) throws Exception;
}
