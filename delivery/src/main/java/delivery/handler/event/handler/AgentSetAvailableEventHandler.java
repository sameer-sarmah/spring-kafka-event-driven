package delivery.handler.event.handler;

import core.commands.delivery.ProcessPendingDeliveryCommand;
import core.events.delivery.AgentSetAvailableEvent;
import core.events.delivery.IDeliveryEvent;
import core.events.handler.IDeliveryEventHandler;
import delivery.repository.DeliveryAgentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class AgentSetAvailableEventHandler implements IDeliveryEventHandler {

    private static final Logger logger = LoggerFactory.getLogger(AgentSetAvailableEventHandler.class);

    @Value(value = "${kafka.command.topic}")
    private String commandTopic;

    @Qualifier("commandKafkaTemplate")
    @Autowired
    private KafkaTemplate<Long, Object> commandKafkaTemplate;

    @Autowired
    private DeliveryAgentRepository deliveryAgentRepository;

    @Override
    public boolean canHandle(IDeliveryEvent deliveryEvent) {
        return deliveryEvent instanceof AgentSetAvailableEvent;
    }

    @Override
    public void handle(IDeliveryEvent deliveryEvent) {
        if(deliveryEvent instanceof AgentSetAvailableEvent){
            AgentSetAvailableEvent agentSetAvailableEvent = (AgentSetAvailableEvent)deliveryEvent;
            publishProcessPendingDeliveryCommand();
            logger.info("ProcessPendingDeliveryCommand published ");
        }
    }

    private void publishProcessPendingDeliveryCommand(){
        ProcessPendingDeliveryCommand processPendingDeliveryCommand = new ProcessPendingDeliveryCommand();
        commandKafkaTemplate.send(commandTopic,processPendingDeliveryCommand);
    }
}
