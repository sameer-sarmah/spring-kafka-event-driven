package delivery.handler.command.handler;

import core.commands.delivery.IDeliveryCommand;
import core.commands.delivery.ProcessPendingDeliveryCommand;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ProcessPendingDeliveryCommandHandler implements IDeliveryCommandHandler {

    private static final Logger logger = LoggerFactory.getLogger(ProcessPendingDeliveryCommandHandler.class);

    @Autowired
    private PendingDeliveryRepository pendingDeliveryRepository;

    @Autowired
    private DeliveryService deliveryService;

    @Value(value = "${kafka.event.topic}")
    private String eventTopic;

    @Qualifier("eventKafkaTemplate")
    @Autowired
    private KafkaTemplate<Long, Object> eventKafkaTemplate;

    @Override
    public boolean canHandle(IDeliveryCommand deliveryCommand) {
        return deliveryCommand instanceof ProcessPendingDeliveryCommand;
    }

    @Override
    public void handle(IDeliveryCommand deliveryCommand) {
        if (deliveryCommand instanceof ProcessPendingDeliveryCommand) {
            ProcessPendingDeliveryCommand processPendingDeliveryCommand = (ProcessPendingDeliveryCommand) deliveryCommand;
            Page<PendingDelivery> pendingDeliveries = pendingDeliveryRepository.findAll(PageRequest.of(0, 1));
            Optional<PendingDelivery> pendingDeliveryOptional = pendingDeliveries.get().findFirst();
            if (pendingDeliveryOptional.isPresent()) {
                PendingDelivery pendingDelivery = pendingDeliveryOptional.get();

                long agentId = deliveryService.getAndAssignAgent();
                if (agentId != -1) {
                    AgentAssignedEvent agentAssignedEvent = new AgentAssignedEvent();
                    agentAssignedEvent.setAgentId(agentId);
                    agentAssignedEvent.setCustomerId(pendingDelivery.getCustomerId());
                    agentAssignedEvent.setOrderId(pendingDelivery.getOrderId());
                    agentAssignedEvent.setRestaurantId(pendingDelivery.getRestaurantId());
                    agentAssignedEvent.setDeliveryAddress(DeliveryUtil.convertAddressEntityToAddressModel(pendingDelivery.getDeliveryAddress()));
                    eventKafkaTemplate.send(eventTopic,agentAssignedEvent.getOrderId(),agentAssignedEvent);
                    logger.info("AgentAssignedEvent successfully published for order id: "+agentAssignedEvent.getOrderId());

                }

            }
        }
    }
}
