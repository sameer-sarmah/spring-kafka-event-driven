package payment.publisher;

import core.commands.payment.CreateAccountCommand;
import core.commands.payment.TransferMoneyCommand;
import core.models.Address;
import core.models.PaymentAccount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import payment.entities.Account;
import payment.repository.AccountRepository;

import java.math.BigDecimal;

@Component
public class CommandProducer {
    @Value(value = "${kafka.command.topic}")
    private String commandTopic;

    @Qualifier("commandKafkaTemplate")
    @Autowired
    private KafkaTemplate<Long, Object> kafkaTemplate;

    @Autowired
    private AccountRepository accountRepository;

    private static final Logger logger = LoggerFactory.getLogger(CommandProducer.class);

    public void publishCreateAccountCommand() {
        CreateAccountCommand createAccountCommand = new CreateAccountCommand();
        Address address = new Address();
        address.setAddress("DSR Spring beauty Apt,Brookfield");
        address.setCity("Bangalore");
        address.setCountry("India");
        address.setPhone("1234");
        address.setState("Karnataka");
        address.setZip("560037");
        createAccountCommand.setBalance(new BigDecimal(1000));
        PaymentAccount paymentAccount = new PaymentAccount();
        paymentAccount.setAddress(address);
        paymentAccount.setEmail("sam@gmail.com");
        paymentAccount.setName("sameer");
        paymentAccount.setPhone("214");
        createAccountCommand.setPaymentAccount(paymentAccount);
        kafkaTemplate.send(commandTopic, createAccountCommand);
        kafkaTemplate.flush();
        logger.info("CreateAccountCommand is published for " + createAccountCommand.getPaymentAccount().getName());

        createAccountCommand = new CreateAccountCommand();
        address = new Address();
        address.setAddress("Moriz Restaurant,Brookfield");
        address.setCity("Bangalore");
        address.setCountry("India");
        address.setPhone("458878");
        address.setState("Karnataka");
        address.setZip("560037");
        createAccountCommand.setBalance(new BigDecimal(10000));
        paymentAccount = new PaymentAccount();
        paymentAccount.setAddress(address);
        paymentAccount.setEmail("moriz.restaurant@gmail.com");
        paymentAccount.setName("Moriz Restaurant");
        paymentAccount.setPhone("4363");
        createAccountCommand.setPaymentAccount(paymentAccount);
        kafkaTemplate.send(commandTopic, createAccountCommand);
        kafkaTemplate.flush();
        logger.info("CreateAccountCommand is published for " + createAccountCommand.getPaymentAccount().getName());
    }

    public void publishTransferMoneyCommand(){
        Account toAccount = accountRepository.findByName("Moriz Restaurant");
        Account fromAccount = accountRepository.findByName("sameer");
        TransferMoneyCommand transferMoneyCommand = new TransferMoneyCommand();
        transferMoneyCommand.setFrom(fromAccount.getId());
        transferMoneyCommand.setTo(toAccount.getId());
        transferMoneyCommand.setAmount(new BigDecimal(100));
        kafkaTemplate.send(commandTopic, transferMoneyCommand);
        kafkaTemplate.flush();
        logger.info("TransferMoneyCommand is published " );

    }

}
