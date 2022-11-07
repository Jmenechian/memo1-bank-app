package com.aninfo.service;

import com.aninfo.exceptions.DepositNegativeSumException;
import com.aninfo.exceptions.InsufficientFundsException;
import com.aninfo.exceptions.InvalidTransactionTypeException;
import com.aninfo.exceptions.WithdrawNegativeSumException;
import com.aninfo.model.Account;
import com.aninfo.model.Transaction;
import com.aninfo.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Collection;
import java.util.Optional;

@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private TransactionService transactionService;

    public Account createAccount(Account account) {
        return accountRepository.save(account);
    }

    public Collection<Account> getAccounts() {
        return accountRepository.findAll();
    }

    public Optional<Account> findById(Long cbu) {
        return accountRepository.findById(cbu);
    }

    public void save(Account account) {
        accountRepository.save(account);
    }

    public void deleteById(Long cbu) {
        accountRepository.deleteById(cbu);
    }

    @Transactional
    public Account withdraw(Long cbu, Double sum) { //modifica balance de cuenta y guarda
        Account account = accountRepository.findAccountByCbu(cbu);

        if (account.getBalance() < sum) {
            throw new InsufficientFundsException("Insufficient funds");
        } else if (sum <= 0) {
            throw new WithdrawNegativeSumException("Can't withdraw negative or nil sums");
        }

        account.setBalance(account.getBalance() - sum);
        accountRepository.save(account);

        return account;
    }

    @Transactional
    public Account deposit(Long cbu, Double sum) {

        if (sum <= 0) {
            throw new DepositNegativeSumException("Can't deposit negative or nil sums");
        }

        Account account = accountRepository.findAccountByCbu(cbu);
        sum = transactionService.applyPromo(sum); //se aplica la promocion
        account.setBalance(account.getBalance() + sum);
        accountRepository.save(account);

        return account;
    }

    public Transaction createDeposit(Transaction transaction) {//crea transaccion tipo deposito
        Optional<Account> account = accountRepository.findById(transaction.getAccountCbu()); //revisa que exista cuenta con cbu

        if (account.isEmpty()){
            throw new InvalidTransactionTypeException("Account cbu doesn't exist");
        }

        deposit(account.get().getCbu(), transaction.getAmount()); //actualiza balance de cuenta
        return transactionService.createDeposit(transaction); //devuelve el deposito ya creado
    }

    public Transaction createWithdraw(Transaction transaction) {//mismo que createDeposit pero con Withdraw
        Optional<Account> optionalAccount = accountRepository.findById(transaction.getAccountCbu());

        if (optionalAccount.isEmpty()){
            throw new InvalidTransactionTypeException("Account cbu doesn't exist");
        }

        withdraw(optionalAccount.get().getCbu(), transaction.getAmount()); //ver si se puede mergear con createWithdraw

        return transactionService.createWithdraw(transaction);
    }

    public Collection<Transaction> getTransactionsFrom(Long cbu) {//devuelve coleccion con todas las transacciones de una cuenta
        return transactionService.getTransactionsFrom((cbu));
    }

    public Optional<Transaction> getTransaction(Long transactionId) {//devuelve transaccion particular por Id
        Optional<Transaction> transaction = transactionService.getTransaction(transactionId); //revisa que exista transaccion con ese id

        if (transaction.isEmpty()) {throw new InvalidTransactionTypeException("Transaction doesn't exist");}

        return transaction;
    }

    public void deleteTransaction(Long transactionId) {
        Optional<Transaction> transactionToDelete = transactionService.getTransaction(transactionId);

        if (transactionToDelete.isEmpty()) {throw new InvalidTransactionTypeException("Transaction doesn't exist");}

        restoreOldBalance(transactionToDelete); //arregla el balance de la cuenta

        transactionService.deleteTransaction(transactionId); //borra la transaccion de la coleccion
    }

    private void restoreOldBalance(Optional<Transaction> transactionToDelete) {
        Double amount = transactionToDelete.get().getAmount();

        Long accountCbu = transactionToDelete.get().getAccountCbu();

        Account account = accountRepository.findAccountByCbu(accountCbu);
        Double oldAccountBalance = account.getBalance();

        account.setBalance(oldAccountBalance - amount);
    }
}
