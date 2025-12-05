package com.bank.user;

public class Banker extends User {
    public Banker(){}
    public Banker(String id, String name, String password, Role role) {
        super(id, name, password, Role.Banker);
    }
}
