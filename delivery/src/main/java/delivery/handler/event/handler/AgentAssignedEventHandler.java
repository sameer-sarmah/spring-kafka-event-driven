package delivery.handler.event.handler;

import core.events.delivery.AgentAssignedEvent;
import core.events.delivery.IDeliveryEvent;
import core.events.delivery.OrderPickedUpEvent;
import core.events.handler.IDeliveryEventHandler;
import delivery.entities.Delivery;
import delivery.entities.DeliveryAgent;
import delivery.entities.DeliveryStatus;
import delivery.repository.DeliveryAgentRepository;
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
public class AgentAssignedEventHandler implements IDeliveryEventHandler {

    private static final Logger logger = LoggerFactory.getLogger(AgentAssignedEventHandler.class);

    @Value(value = "${kafka.event.topic}")
    private String eventTopic;

    @Qualifier("eventKafkaTemplate")
    @Autowired
    private KafkaTemplate<Long, Object> eventKafkaTemplate;

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Autowired
    private DeliveryAgentRepository deliveryAgentRepository;

    @Override
    public boolean canHandle(IDeliveryEvent deliveryEvent) {
        return deliveryEvent instanceof AgentAssignedEvent;
    }

    @Override
    public void handle(IDeliveryEvent deliveryEvent) {
        if (deliveryEvent instanceof AgentAssignedEvent) {
            AgentAssignedEvent agentAssignedEvent = (AgentAssignedEvent) deliveryEvent;
            Optional<Delivery>  deliveryOptional = deliveryRepository.findByOrderId(agentAssignedEvent.getOrderId());
            Optional<DeliveryAgent> deliveryAgentOptional = deliveryAgentRepository.findById(agentAssignedEvent.getAgentId());
            if(deliveryOptional.isPresent() && deliveryAgentOptional.isPresent()){
                Delivery delivery = deliveryOptional.get();
                DeliveryAgent deliveryAgent = deliveryAgentOptional.get();
                delivery.setDeliveryAgent(deliveryAgent);
                delivery.setDeliveryStatus(DeliveryStatus.AGENT_ASSIGNED);
                deliveryRepository.saveAndFlush(delivery);
                logger.info("Delivery is persisted with status AGENT_ASSIGNED for order id:"+delivery.getOrderId());
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                delivery.setDeliveryStatus(DeliveryStatus.PICKED_UP);
                delivery.setPickedUpAt(LocalDateTime.now());
                deliveryRepository.saveAndFlush(delivery);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                publishOrderPickedUpEvent(agentAssignedEvent);
                logger.info("OrderPickedUpEvent published for order id: "+agentAssignedEvent.getOrderId());
            }
        }
    }

    private void publishOrderPickedUpEvent(AgentAssignedEvent agentAssignedEvent){
        OrderPickedUpEvent orderPickedUpEvent = new OrderPickedUpEvent();
        orderPickedUpEvent.setCustomerId(agentAssignedEvent.getCustomerId());
        orderPickedUpEvent.setDeliveryAddress(agentAssignedEvent.getDeliveryAddress());
        orderPickedUpEvent.setDeliveryAgentId(agentAssignedEvent.getAgentId());
        orderPickedUpEvent.setOrderId(agentAssignedEvent.getOrderId());
        orderPickedUpEvent.setAgentId(agentAssignedEvent.getAgentId());
        eventKafkaTemplate.send(eventTopic,orderPickedUpEvent.getOrderId(),orderPickedUpEvent);
    }
}
