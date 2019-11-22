package core.commands.delivery;

import core.models.Address;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PickupOrderCommand  implements  IDeliveryCommand{
    private String type = PickupOrderCommand.class.getName();
    private long customerId;
    private long restaurantId;
    @TargetAggregateIdentifier
    private long orderId;
    private Address deliveryAddress;
}
