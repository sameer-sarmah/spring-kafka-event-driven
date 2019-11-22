package core.events.delivery;

import core.commands.delivery.AssignAgentCommand;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PendingDeliveryProcessedEvent implements IDeliveryEvent {
    private String type = PendingDeliveryProcessedEvent.class.getName();
}
