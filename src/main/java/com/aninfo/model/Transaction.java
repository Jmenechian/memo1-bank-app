package com.aninfo.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)

    private Long transactionId;
    private Long accountCbu;
    private Double amount;

    public Transaction(){}

    public void setAmount(Double amount) {this.amount = amount;}
    public Double getAmount() {return amount;}

    public Long getTransactionId(){return transactionId;}
    public Long getAccountCbu() {return accountCbu;}
}
