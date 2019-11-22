package delivery.handler.command.handler;

import core.commands.delivery.IDeliveryCommand;
import core.commands.handler.IDeliveryCommandHandler;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DeliveryCommandHandler {
    @Autowired
    List<IDeliveryCommandHandler> deliveryCommandHandlers;

//    @Qualifier("commandRebalanceListener")
//    @Autowired
//    private RebalanceListener rebalanceListener;

    @KafkaListener(topics = "#{'${kafka.command.topic}'}", groupId = "#{'${kafka.command.consumer.group}'}" , containerFactory = "kafkaCommandListenerContainerFactory")
    public void handle(@Payload ConsumerRecord message,Consumer<Long,Object> consumer){
        if(message.value() != null && message.value() instanceof IDeliveryCommand){
            IDeliveryCommand event = (IDeliveryCommand)message.value();
            for(IDeliveryCommandHandler commandHandler : deliveryCommandHandlers){
                if(commandHandler.canHandle(event)){
                    commandHandler.handle(event);
                    consumer.commitSync();
                   // rebalanceListener.addOffset(message.topic(),message.partition(),message.offset());
                }
            }
        }

    }
}
