package delivery.handler.event.handler;

import core.events.delivery.IDeliveryEvent;
import core.events.handler.IDeliveryEventHandler;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DeliveryEventHandler {

    @Autowired
    List<IDeliveryEventHandler> deliveryEventHandlers;

//    @Qualifier("eventRebalanceListener")
//    @Autowired
//    private RebalanceListener rebalanceListener;

    @KafkaListener(topics = "#{'${kafka.event.topic}'}", groupId = "#{'${kafka.event.consumer.group}'}",containerFactory = "kafkaEventListenerContainerFactory")
    public void handle(@Payload ConsumerRecord message,Consumer<Long,Object> consumer){
        if(message.value() != null && message.value() instanceof IDeliveryEvent){
            IDeliveryEvent event = (IDeliveryEvent)message.value();
            for(IDeliveryEventHandler eventHandler : deliveryEventHandlers){
                if(eventHandler.canHandle(event)){
                    eventHandler.handle(event);
                   // rebalanceListener.addOffset(message.topic(),message.partition(),message.offset());
                    consumer.commitSync();
                }
            }
        }
    }
}
