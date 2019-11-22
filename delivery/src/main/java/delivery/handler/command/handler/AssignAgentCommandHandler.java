package delivery.handler.command.handler;

import core.commands.delivery.AssignAgentCommand;
import core.commands.delivery.IDeliveryCommand;
import core.commands.handler.IDeliveryCommandHandler;
import core.events.delivery.AgentAssignedEvent;
import delivery.entities.PendingDelivery;
import delivery.repository.PendingDeliveryRepository;
import delivery.service.DeliveryService;
import delivery.util.DeliveryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class AssignAgentCommandHandler implements IDeliveryCommandHandler {

    private static final Logger logger = LoggerFactory.getLogger(AssignAgentCommandHandler.class);

    @Value(value = "${kafka.event.topic}")
    private String eventTopic;

    @Qualifier("eventKafkaTemplate")
    @Autowired
    private KafkaTemplate<Long, Object> eventKafkaTemplate;

    @Autowired
    private PendingDeliveryRepository pendingDeliveryRepository;

    @Autowired
    private DeliveryService deliveryService;

    @Override
    public boolean canHandle(IDeliveryCommand deliveryCommand) {
        return deliveryCommand instanceof AssignAgentCommand;
    }

    @Override
    public void handle(IDeliveryCommand deliveryCommand) {
        if (deliveryCommand instanceof AssignAgentCommand) {
            AssignAgentCommand assignAgentCommand = (AssignAgentCommand)deliveryCommand;
            long agentId = deliveryService.getAndAssignAgent();
            if(agentId != -1){
                AgentAssignedEvent agentAssignedEvent = new AgentAssignedEvent();
                agentAssignedEvent.setAgentId(agentId);
                agentAssignedEvent.setCustomerId(assignAgentCommand.getCustomerId());
                agentAssignedEvent.setOrderId(assignAgentCommand.getOrderId());
                agentAssignedEvent.setRestaurantId(assignAgentCommand.getRestaurantId());
                agentAssignedEvent.setDeliveryAddress(assignAgentCommand.getDeliveryAddress());
                eventKafkaTemplate.send(eventTopic,agentAssignedEvent.getOrderId(),agentAssignedEvent);
                eventKafkaTemplate.flush();
                logger.info("AgentAssignedEvent successfully published for order id: "+agentAssignedEvent.getOrderId());
            }
            else{
                PendingDelivery pendingDelivery = new PendingDelivery();
                pendingDelivery.setCreatedAt(LocalDateTime.now());
                pendingDelivery.setOrderId(assignAgentCommand.getOrderId());
                pendingDelivery.setCustomerId(assignAgentCommand.getCustomerId());
                pendingDelivery.setDeliveryAddress(DeliveryUtil.convertAddressModelToAddressEntity(assignAgentCommand.getDeliveryAddress()));
                pendingDelivery.setRestaurantId(assignAgentCommand.getRestaurantId());
                pendingDeliveryRepository.saveAndFlush(pendingDelivery);
                logger.info("No agent is available, inserting into PendingDelivery table for order id "+pendingDelivery.getOrderId());
            }
        }
    }


}
