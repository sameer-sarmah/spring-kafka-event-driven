package payment.command.handlers;

import core.commands.handler.IPaymentCommandHandler;
import core.commands.payment.CreateAccountCommand;
import core.commands.payment.IPaymentCommand;
import core.events.payment.AccountCreatedEvent;
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
import payment.service.PaymentService;
import payment.util.PaymentUtil;

@Component
public class CreateAccountCommandHandler implements IPaymentCommandHandler {

    private static final Logger logger = LoggerFactory.getLogger(TransferMoneyCommandHandler.class);

    @Autowired
    private PaymentService paymentService;

    @Value(value = "${kafka.event.topic}")
    private String eventTopic;

    @Qualifier("eventKafkaTemplate")
    @Autowired
    private KafkaTemplate<Long, Object> eventKafkaTemplate;

    @Autowired
    private AccountRepository accountRepository;

    @Override
    public boolean canHandle(IPaymentCommand paymentCommand) {
        return paymentCommand instanceof CreateAccountCommand;
    }

    @Override
    public void handle(IPaymentCommand paymentCommand) {
        if(paymentCommand instanceof CreateAccountCommand){
            CreateAccountCommand createAccountCommand = (CreateAccountCommand)paymentCommand;
            PaymentAccount paymentAccount =createAccountCommand.getPaymentAccount();
            Account account = new Account();
            account.setAddress(PaymentUtil.convertAddressModelToAddressEntity(paymentAccount.getAddress()));
            account.setName(paymentAccount.getName());
            account.setEmail(paymentAccount.getEmail());
            account.setBalance(createAccountCommand.getBalance());
            Account savedAccount = accountRepository.save(account);
            savedAccount.getId();
            publishAccountCreatedEvent(savedAccount.getId());
            logger.info("AccountCreatedEvent successfully published");

        }

    }

    private void publishAccountCreatedEvent(long accountId){
        AccountCreatedEvent accountCreatedEvent = new AccountCreatedEvent();
        accountCreatedEvent.setAccountId(accountId);
        eventKafkaTemplate.send(eventTopic,accountCreatedEvent);
    }
}
