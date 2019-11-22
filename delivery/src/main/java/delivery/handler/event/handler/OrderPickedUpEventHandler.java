package delivery.handler.event.handler;

import core.commands.delivery.DeliverOrderCommand;
import core.events.delivery.IDeliveryEvent;
import core.events.delivery.OrderPickedUpEvent;
import core.events.handler.IDeliveryEventHandler;
import delivery.repository.DeliveryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderPickedUpEventHandler implements IDeliveryEventHandler {

    @Value(value = "${kafka.command.topic}")
    private String commandTopic;

    @Qualifier("commandKafkaTemplate")
    @Autowired
    private KafkaTemplate<Long, Object> commandKafkaTemplate;

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Override
    public boolean canHandle(IDeliveryEvent deliveryEvent) {
        return deliveryEvent instanceof OrderPickedUpEvent;
    }

    @Override
    public void handle(IDeliveryEvent deliveryEvent) {
        if (deliveryEvent instanceof OrderPickedUpEvent) {
            OrderPickedUpEvent orderPickedUpEvent = (OrderPickedUpEvent)deliveryEvent;
            publishDeliverOrderCommand(orderPickedUpEvent);
        }
    }

    private void publishDeliverOrderCommand(OrderPickedUpEvent orderPickedUpEvent){
        DeliverOrderCommand deliverOrderCommand = new DeliverOrderCommand();
        deliverOrderCommand.setCustomerId(orderPickedUpEvent.getCustomerId());
        deliverOrderCommand.setAgentId(orderPickedUpEvent.getAgentId());
        deliverOrderCommand.setOrderId(orderPickedUpEvent.getOrderId());
        commandKafkaTemplate.send(commandTopic,orderPickedUpEvent.getOrderId(),deliverOrderCommand);
    }
}
