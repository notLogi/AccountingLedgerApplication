package com.pluralsight;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class Transaction {
    private int transactionId;
    private int userId;
    private LocalDateTime dateTime;
    private String description;
    private String vendor;
    private double amount;

    Transaction(int transactionId, int userId, LocalDateTime dateTime, String description, String vendor, double amount){
        this.transactionId = transactionId;
        this.userId = userId;
        this.dateTime = dateTime;
        this.description = description;
        this.vendor = vendor;
        this.amount = amount;
    }

    public int getTransactionId(){
        return transactionId;
    }
    public int getUserId(){
        return userId;
    }
    public LocalDateTime getDate(){
        return dateTime;
    }
    public void setDateTime(LocalDateTime dateTime){
        this.dateTime = dateTime;
    }
    public String getDescription(){
        return description;
    }
    public void setDescription(String description){
        this.description = description;
    }
    public String getVendor(){
        return vendor;
    }
    public void setVendor(String vendor){
        this.vendor = vendor;
    }
    public double getAmount(){
        return amount;
    }
    public void setAmount(double amount){
        this.amount = amount;
    }

    @Override
    public String toString() {
        return "Transaction: {"+
                "date: " + getDate() +
                ", description: " + getDescription()  +
                ", vendor: " + getVendor()  +
                ", amount: " + getAmount() +
                '}' + "\n";
    }
}
