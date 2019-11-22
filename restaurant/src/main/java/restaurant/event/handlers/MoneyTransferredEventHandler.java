package restaurant.event.handlers;

import core.commands.restaurant.CreateCustomerCommand;
import core.events.handler.IPaymentEventHandler;
import core.events.payment.IPaymentEvent;
import core.events.payment.MoneyTransferredEvent;
import core.events.restaurant.CustomerCreatedEvent;
import core.events.restaurant.OrderPreparationCompletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import restaurant.entities.Order;
import restaurant.entities.OrderState;
import restaurant.respository.OrderRepository;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
public class MoneyTransferredEventHandler implements IPaymentEventHandler {

    private static final Logger logger = LoggerFactory.getLogger(MoneyTransferredEventHandler.class);

    @Value(value = "${kafka.restaurant.event.topic}")
    private String restaurantEventTopic;

    @Qualifier("kafkaTemplate")
    @Autowired
    private KafkaTemplate<Long, Object> kafkaTemplate;

    @Autowired
    private OrderRepository orderRepository;

    @Override
    public boolean canHandle(IPaymentEvent paymentEvent) {
        return paymentEvent instanceof MoneyTransferredEvent;
    }

    @Override
    public void handle(IPaymentEvent paymentEvent) {
        if (paymentEvent instanceof MoneyTransferredEvent) {
            MoneyTransferredEvent moneyTransferredEvent = (MoneyTransferredEvent) paymentEvent;
            Optional<Order> optionalOrder = orderRepository.findById(moneyTransferredEvent.getOrderId());
            if (optionalOrder.isPresent()) {
                Order order = optionalOrder.get();
                order.setOrderStatus(OrderState.ORDER_ACCEPTED);
                orderRepository.saveAndFlush(order);
                logger.info("Order persisted with status ORDER_ACCEPTED for order id: "+moneyTransferredEvent.getOrderId());
                publishOrderPreparationCompletedEvent(moneyTransferredEvent);
                logger.info("OrderPreparationCompletedEvent is published for order id : "+moneyTransferredEvent.getOrderId());
            }
        }
    }

    private void publishOrderPreparationCompletedEvent(MoneyTransferredEvent moneyTransferredEvent){
        OrderPreparationCompletedEvent orderPreparationCompletedEvent = new OrderPreparationCompletedEvent();
        orderPreparationCompletedEvent.setCompletedAt(LocalDateTime.now());
        orderPreparationCompletedEvent.setOrderId(moneyTransferredEvent.getOrderId());
        kafkaTemplate.send(restaurantEventTopic,moneyTransferredEvent.getOrderId(),orderPreparationCompletedEvent);
    }
}
