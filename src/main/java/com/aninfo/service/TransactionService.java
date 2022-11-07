package com.aninfo.service;

import com.aninfo.model.Account;
import com.aninfo.model.Transaction;
import com.aninfo.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    public Transaction createDeposit(Transaction transaction){
        transaction.setAmount(this.applyPromo(transaction.getAmount())); //transaction.getAmount() + extra

        return saveTransaction(transaction);
    }

    public Transaction createWithdraw(Transaction transaction){
        transaction.setAmount(-transaction.getAmount());//monto negativo

        return saveTransaction(transaction);
    }

    private Transaction saveTransaction(Transaction transaction) {
        return this.transactionRepository.save(transaction);
    }

    public Double applyPromo(Double amount) {//calculo de promo
        if (amount >= 2000){
            Double promo = amount * 0.1;

            if (promo > 500) {promo = 500.00;}

            amount += promo;
        }

        return amount;
    }

    public List<Transaction> getTransactionsFrom(Long cbu){
        return this.transactionRepository.findAllByAccountCbu(cbu);
    }

    public Optional<Transaction> getTransaction(Long transactionId){
        return this.transactionRepository.findById(transactionId);
    }

    public void deleteTransaction(Long transactionId){
        transactionRepository.deleteById(transactionId);
    }
}
