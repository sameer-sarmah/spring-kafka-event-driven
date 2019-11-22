package payment.command.handlers;

import core.commands.handler.IPaymentCommandHandler;
import core.commands.payment.IPaymentCommand;
import core.commands.payment.TransferMoneyCommand;
import core.events.payment.MoneyTransferFailedEvent;
import core.events.payment.MoneyTransferredEvent;
import core.exception.AccountNotFound;
import core.exception.InsufficientBalance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import payment.service.PaymentService;

@Component
public class TransferMoneyCommandHandler implements IPaymentCommandHandler {

    private static final Logger logger = LoggerFactory.getLogger(TransferMoneyCommandHandler.class);

    @Autowired
    private PaymentService paymentService;

    @Value(value = "${kafka.event.topic}")
    private String eventTopic;

    @Qualifier("eventKafkaTemplate")
    @Autowired
    private KafkaTemplate<Long, Object> eventKafkaTemplate;

    @Override
    public boolean canHandle(IPaymentCommand paymentCommand) {
        return paymentCommand instanceof TransferMoneyCommand;
    }

    @Override
    public void handle(IPaymentCommand paymentCommand) {
        if(paymentCommand instanceof TransferMoneyCommand){
            TransferMoneyCommand transferMoneyCommand = (TransferMoneyCommand) paymentCommand;
            try {
                paymentService.transferMoney(transferMoneyCommand.getFrom(),transferMoneyCommand.getTo(),transferMoneyCommand.getAmount());
                MoneyTransferredEvent moneyTransferredEvent = new MoneyTransferredEvent();
                moneyTransferredEvent.setAmount(transferMoneyCommand.getAmount());
                moneyTransferredEvent.setFrom(transferMoneyCommand.getFrom());
                moneyTransferredEvent.setTo(transferMoneyCommand.getTo());
                moneyTransferredEvent.setOrderId(transferMoneyCommand.getOrderId());
                eventKafkaTemplate.send(eventTopic,moneyTransferredEvent.getOrderId(),moneyTransferredEvent);
                logger.info("MoneyTransferredEvent successfully published for order id: "+moneyTransferredEvent.getOrderId());
            } catch (InsufficientBalance | AccountNotFound exception) {
                MoneyTransferFailedEvent moneyTransferFailedEvent = new MoneyTransferFailedEvent();
                moneyTransferFailedEvent.setAmount(transferMoneyCommand.getAmount());
                moneyTransferFailedEvent.setFrom(transferMoneyCommand.getFrom());
                moneyTransferFailedEvent.setTo(transferMoneyCommand.getTo());
                moneyTransferFailedEvent.setOrderId(transferMoneyCommand.getOrderId());
                eventKafkaTemplate.send(eventTopic,moneyTransferFailedEvent.getOrderId(),moneyTransferFailedEvent);
                logger.error("MoneyTransferredEvent successfully published for order id: "+moneyTransferFailedEvent.getOrderId());

            }
        }
    }
}
