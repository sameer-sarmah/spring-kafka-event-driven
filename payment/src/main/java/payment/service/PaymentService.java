package payment.service;

import core.exception.AccountNotFound;
import core.exception.InsufficientBalance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import payment.entities.Account;
import payment.repository.AccountRepository;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.Optional;

@Service
public class PaymentService {

    @Autowired
    private AccountRepository accountRepository;

    @Transactional
    public synchronized void transferMoney(long from, long to, BigDecimal amount) throws InsufficientBalance,AccountNotFound {
        Optional<Account> fromAccountOptional = accountRepository.findById(from);
        if(fromAccountOptional.isPresent()){
           Account fromAccount =  fromAccountOptional.get();
           if(fromAccount.getBalance().compareTo(amount) >= 0){
               Optional<Account> toAccountOptional = accountRepository.findById(to);
               if(toAccountOptional.isPresent()){
                   Account toAccount =  toAccountOptional.get();
                   fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
                   toAccount.setBalance(toAccount.getBalance().add(amount));
                   accountRepository.save(fromAccount);
                   accountRepository.save(toAccount);
                   accountRepository.flush();
               }
               else{
                   throw new AccountNotFound();
               }
           }
           else {
               throw new InsufficientBalance();
           }
        }
        else {
            throw new AccountNotFound();
        }
    }
}
