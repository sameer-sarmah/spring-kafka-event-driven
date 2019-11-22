package delivery.handler.command.handler;

import core.commands.delivery.DeliverOrderCommand;
import core.commands.delivery.IDeliveryCommand;
import core.commands.handler.IDeliveryCommandHandler;
import core.events.delivery.OrderDeliveredEvent;
import delivery.entities.Delivery;
import delivery.entities.DeliveryStatus;
import delivery.repository.DeliveryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class DeliverOrderCommandHandler implements IDeliveryCommandHandler {

    private static final Logger logger = LoggerFactory.getLogger(DeliverOrderCommandHandler.class);

    @Value(value = "${kafka.event.topic}")
    private String eventTopic;

    @Qualifier("eventKafkaTemplate")
    @Autowired
    private KafkaTemplate<Long, Object> eventKafkaTemplate;

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Override
    public boolean canHandle(IDeliveryCommand deliveryCommand) {
        return deliveryCommand instanceof DeliverOrderCommand;
    }

    @Override
    public void handle(IDeliveryCommand deliveryCommand) {
        if(deliveryCommand instanceof DeliverOrderCommand){
            DeliverOrderCommand deliverOrderCommand = (DeliverOrderCommand)deliveryCommand;
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Optional<Delivery> deliveryOptional = deliveryRepository.findByOrderId(deliverOrderCommand.getOrderId());
            if(deliveryOptional.isPresent()){
                Delivery delivery = deliveryOptional.get();
                delivery.setDeliveryStatus(DeliveryStatus.DELIVERED);
                delivery.setDeliveredAt(LocalDateTime.now());
                deliveryRepository.saveAndFlush(delivery);
                logger.info("Delivery is persisted with status DELIVERED for order id:"+delivery.getOrderId());

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            publishOrderDeliveredEvent(deliverOrderCommand);
            logger.info("DeliverOrderCommand is processed and OrderDeliveredEvent is published for order id "+deliverOrderCommand.getOrderId());
        }
    }

    private void publishOrderDeliveredEvent(DeliverOrderCommand deliverOrderCommand){
        OrderDeliveredEvent orderDeliveredEvent = new OrderDeliveredEvent();
        orderDeliveredEvent.setCustomerId(deliverOrderCommand.getCustomerId());
        orderDeliveredEvent.setAgentId(deliverOrderCommand.getAgentId());
        orderDeliveredEvent.setOrderId(deliverOrderCommand.getOrderId());
        eventKafkaTemplate.send(eventTopic,deliverOrderCommand.getOrderId(),orderDeliveredEvent);
    }
}
