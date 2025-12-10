package com.bank.accounts;

public class CardType {
    public enum DebitCardType{
        Platinum_Mastercard,
        Titanium_Mastercard,
        Standard_Mastercard
    }

    protected DebitCardType type;

    public CardType(DebitCardType type) {
        this.type = type;
    }

    public DebitCardType getCardType() {
        return type;
    }

    public void setCardType(DebitCardType type) {
        this.type = type;
    }


    private double withdrawLimitPerDay(){
        switch (type){
            case Platinum_Mastercard: return 20000;
            case Titanium_Mastercard: return 10000;
            default: return 5000;
        }
    }

    private double getTransferLimitPerDay(){
        switch (type){
            case Platinum_Mastercard: return 40000;
            case Titanium_Mastercard: return 20000;
            default: return 10000;
        }
    }

    private double getTransferOwnLimitPerDay(){
        switch (type){
            case Platinum_Mastercard: return 80000;
            case Titanium_Mastercard: return 40000;
            default: return 20000;
        }
    }

    private double getDepositeLimitPerDay(){
        return 100000;

    }

//    private double getDepositeOwnLimitPerDay(){
//        return 200000;
//    }


    public double dailyWithdrawLimit() {
        return withdrawLimitPerDay();
    }

    public double dailyDepositLimit() {
        return getDepositeLimitPerDay();
    }

    public double dailyTransferLimit() {
        return getTransferLimitPerDay();
    }

    public double dailyOwnTransferLimit() {
        return getTransferOwnLimitPerDay();
    }

//    public double dailyOwnDepositLimit() {
//        return getDepositeOwnLimitPerDay();
//    }



}
