package core.events.delivery;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AgentSetAvailableEvent implements IDeliveryEvent{
    private String type = AgentSetAvailableEvent.class.getName();
    private long agentId;
}
