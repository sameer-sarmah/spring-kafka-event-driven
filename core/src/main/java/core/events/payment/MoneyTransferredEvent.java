package core.events.payment;

import core.commands.payment.TransferMoneyCommand;
import core.models.Money;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class MoneyTransferredEvent {
    private String type = MoneyTransferredEvent.class.getName();
    private Long orderId;
    private Long from;
    private Long to;
    private BigDecimal amount;
}
