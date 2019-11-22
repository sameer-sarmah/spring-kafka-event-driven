package restaurant.event.handlers;

import core.events.handler.IPaymentEventHandler;
import core.events.payment.IPaymentEvent;
import core.events.restaurant.OrderPreparationCompletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import restaurant.command.publisher.CommandProducer;
import restaurant.entities.Order;
import restaurant.respository.OrderRepository;

import java.util.Optional;

public class OrderPreparationCompletedEventHandler implements IPaymentEventHandler {
    private static final Logger logger = LoggerFactory.getLogger(OrderPreparationCompletedEventHandler.class);

    @Autowired
    private CommandProducer commandProducer;

    @Autowired
    private OrderRepository orderRepository;

    @Override
    public boolean canHandle(IPaymentEvent paymentEvent) {
        return paymentEvent instanceof OrderPreparationCompletedEvent;
    }

    @Override
    public void handle(IPaymentEvent paymentEvent) {
        if(paymentEvent instanceof OrderPreparationCompletedEvent){
            OrderPreparationCompletedEvent orderPreparationCompletedEvent = (OrderPreparationCompletedEvent)paymentEvent;
            Optional<Order> optionalOrder = orderRepository.findById(orderPreparationCompletedEvent.getOrderId());
            if (optionalOrder.isPresent()) {
                Order order = optionalOrder.get();
                commandProducer.publishPickupOrderCommand(order.getId(),order.getCustomer().getId(),order.getRestaurant().getId());
            }
        }
    }
}
