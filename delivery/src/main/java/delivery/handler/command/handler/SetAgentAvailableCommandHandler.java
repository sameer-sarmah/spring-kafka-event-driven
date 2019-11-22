package delivery.handler.command.handler;

import core.commands.delivery.IDeliveryCommand;
import core.commands.delivery.SetAgentAvailableCommand;
import core.commands.handler.IDeliveryCommandHandler;
import core.events.delivery.AgentSetAvailableEvent;
import delivery.entities.DeliveryAgent;
import delivery.repository.DeliveryAgentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SetAgentAvailableCommandHandler implements IDeliveryCommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(SetAgentAvailableCommandHandler.class);

    @Qualifier("eventKafkaTemplate")
    @Autowired
    private KafkaTemplate<Long, Object> eventKafkaTemplate;

    @Value(value = "${kafka.event.topic}")
    private String eventTopic;

    @Autowired
    private DeliveryAgentRepository deliveryAgentRepository;

    @Override
    public boolean canHandle(IDeliveryCommand deliveryCommand) {
        return deliveryCommand instanceof SetAgentAvailableCommand;
    }

    @Override
    public void handle(IDeliveryCommand deliveryCommand) {
        if(deliveryCommand instanceof SetAgentAvailableCommand){
            SetAgentAvailableCommand setAgentAvailableCommand = (SetAgentAvailableCommand)deliveryCommand;
            Optional<DeliveryAgent> deliveryAgentOptional = deliveryAgentRepository.findById(setAgentAvailableCommand.getAgentId());
            if(deliveryAgentOptional.isPresent()){
                DeliveryAgent deliveryAgent =deliveryAgentOptional.get();
                deliveryAgent.setAvailable(true);
                deliveryAgentRepository.saveAndFlush(deliveryAgent);
                logger.info("DeliveryAgent is persisted,availability set to true for agent id:"+deliveryAgent.getId());
            }
            publishAgentSetAvailableEvent(setAgentAvailableCommand);
            logger.info("AgentSetAvailableEvent is published for agent id:"+setAgentAvailableCommand.getAgentId());
        }
    }

    private void publishAgentSetAvailableEvent(SetAgentAvailableCommand setAgentAvailableCommand){
        AgentSetAvailableEvent agentSetAvailableEvent = new AgentSetAvailableEvent();
        agentSetAvailableEvent.setAgentId(setAgentAvailableCommand.getAgentId());
        eventKafkaTemplate.send(eventTopic,agentSetAvailableEvent);
    }
}
