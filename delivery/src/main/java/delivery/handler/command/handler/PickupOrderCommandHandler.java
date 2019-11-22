package delivery.handler.command.handler;

import core.commands.delivery.AssignAgentCommand;
import core.commands.delivery.IDeliveryCommand;
import core.commands.delivery.PickupOrderCommand;
import core.commands.handler.IDeliveryCommandHandler;
import delivery.entities.Delivery;
import delivery.entities.DeliveryStatus;
import delivery.repository.DeliveryRepository;
import delivery.util.DeliveryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PickupOrderCommandHandler implements IDeliveryCommandHandler {

    private static final Logger logger = LoggerFactory.getLogger(PickupOrderCommandHandler.class);

    @Value(value = "${kafka.command.topic}")
    private String commandTopic;

    @Qualifier("commandKafkaTemplate")
    @Autowired
    private KafkaTemplate<Long, Object> commandKafkaTemplate;

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Override
    public boolean canHandle(IDeliveryCommand deliveryCommand) {
        return deliveryCommand instanceof PickupOrderCommand;
    }

    @Override
    public void handle(IDeliveryCommand deliveryCommand) {
        if(deliveryCommand instanceof PickupOrderCommand){
            PickupOrderCommand pickupOrderCommand = (PickupOrderCommand)deliveryCommand;
            deliveryRepository.saveAndFlush(populateDelivery(pickupOrderCommand));
            publishAssignAgentCommand(pickupOrderCommand);
            logger.info("AssignAgentCommand is published for order id: " +pickupOrderCommand.getOrderId());
        }
    }

    private void publishAssignAgentCommand(PickupOrderCommand pickupOrderCommand){
        AssignAgentCommand assignAgentCommand = new AssignAgentCommand();
        assignAgentCommand.setRestaurantId(pickupOrderCommand.getRestaurantId());
        assignAgentCommand.setCustomerId(pickupOrderCommand.getCustomerId());
        assignAgentCommand.setOrderId(pickupOrderCommand.getOrderId());
        assignAgentCommand.setDeliveryAddress(pickupOrderCommand.getDeliveryAddress());
        commandKafkaTemplate.send(commandTopic, assignAgentCommand.getOrderId(), assignAgentCommand);
        commandKafkaTemplate.flush();
    }

    private Delivery populateDelivery(PickupOrderCommand pickupOrderCommand){
        Delivery delivery = new Delivery();
        delivery.setDeliveryAddress(DeliveryUtil.convertAddressModelToAddressEntity(pickupOrderCommand.getDeliveryAddress()));
        delivery.setOrderId(pickupOrderCommand.getOrderId());
        delivery.setCustomerId(pickupOrderCommand.getCustomerId());
        delivery.setDeliveryStatus(DeliveryStatus.AGENT_NOT_ASSIGNED);
        return delivery;
    }
}
