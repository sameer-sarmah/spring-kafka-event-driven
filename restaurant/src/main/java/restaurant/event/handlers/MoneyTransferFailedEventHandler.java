package restaurant.event.handlers;

import core.events.delivery.OrderDeliveredEvent;
import core.events.handler.IPaymentEventHandler;
import core.events.payment.IPaymentEvent;
import core.events.payment.MoneyTransferFailedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import restaurant.entities.Order;
import restaurant.entities.OrderState;
import restaurant.respository.OrderRepository;

import java.util.Optional;

@Component
public class MoneyTransferFailedEventHandler implements IPaymentEventHandler {

    private static final Logger logger = LoggerFactory.getLogger(MoneyTransferFailedEventHandler.class);

    @Autowired
    private OrderRepository orderRepository;

    @Override
    public boolean canHandle(IPaymentEvent paymentEvent) {
        return paymentEvent instanceof MoneyTransferFailedEvent;
    }

    @Override
    public void handle(IPaymentEvent paymentEvent) {
        if(paymentEvent instanceof MoneyTransferFailedEvent) {
            MoneyTransferFailedEvent moneyTransferFailedEvent = (MoneyTransferFailedEvent) paymentEvent;
            Optional<Order> optionalOrder = orderRepository.findById(moneyTransferFailedEvent.getOrderId());
            if (optionalOrder.isPresent()) {
                Order order = optionalOrder.get();
                order.setOrderStatus(OrderState.ORDER_REJECTED);
                orderRepository.saveAndFlush(order);
                logger.info("Order persisted with status ORDER_REJECTED for order id: "+moneyTransferFailedEvent.getOrderId());
            }
        }
    }
}
