package payment.command.handlers;

import core.commands.handler.IPaymentCommandHandler;
import core.commands.payment.IPaymentCommand;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PaymentCommandHandler {
    @Autowired
    List<IPaymentCommandHandler> paymentCommandHandlers;

    @KafkaListener(topics = "#{'${kafka.command.topic}'}", groupId = "#{'${kafka.command.consumer.group}'}" , containerFactory = "kafkaCommandListenerContainerFactory")
    public void handle(@Payload ConsumerRecord message,Consumer<Long,Object> consumer){
        if(message.value() != null && message.value() instanceof IPaymentCommand){
            IPaymentCommand event = (IPaymentCommand)message.value();
            for(IPaymentCommandHandler commandHandler : paymentCommandHandlers){
                if(commandHandler.canHandle(event)){
                    commandHandler.handle(event);
                    consumer.commitSync();
                }
            }
        }

    }
}
