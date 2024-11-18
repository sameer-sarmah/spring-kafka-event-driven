package core.commands.delivery;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import core.models.Address;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeliverOrderCommand  implements  IDeliveryCommand{
    private String type = DeliverOrderCommand.class.getName();
    private long customerId;
    private long orderId;
    private long agentId;
    private Address deliveryAddress;
}
