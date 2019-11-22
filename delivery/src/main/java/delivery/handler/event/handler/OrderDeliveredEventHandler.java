package delivery.handler.event.handler;

import core.commands.delivery.SetAgentAvailableCommand;
import core.events.delivery.IDeliveryEvent;
import core.events.delivery.OrderDeliveredEvent;
import core.events.handler.IDeliveryEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderDeliveredEventHandler implements IDeliveryEventHandler {

    private static final Logger logger = LoggerFactory.getLogger(OrderDeliveredEventHandler.class);

    @Value(value = "${kafka.command.topic}")
    private String commandTopic;

    @Qualifier("commandKafkaTemplate")
    @Autowired
    private KafkaTemplate<Long, Object> commandKafkaTemplate;

    @Override
    public boolean canHandle(IDeliveryEvent deliveryEvent) {
        return deliveryEvent instanceof OrderDeliveredEvent;
    }

    @Override
    public void handle(IDeliveryEvent deliveryEvent) {
        if(deliveryEvent instanceof OrderDeliveredEvent){
            OrderDeliveredEvent orderDeliveredEvent = (OrderDeliveredEvent)deliveryEvent;
            publishSetAgentAvailableCommand(orderDeliveredEvent);
            logger.info("SetAgentAvailableCommand published for agent id: "+orderDeliveredEvent.getAgentId());
        }
    }

    private void publishSetAgentAvailableCommand(OrderDeliveredEvent orderDeliveredEvent) {
        SetAgentAvailableCommand setAgentAvailableCommand = new SetAgentAvailableCommand();
        setAgentAvailableCommand.setAgentId(orderDeliveredEvent.getAgentId());
        commandKafkaTemplate.send(commandTopic,orderDeliveredEvent.getOrderId(),setAgentAvailableCommand);
    }
}
