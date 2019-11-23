package restaurant.command.publisher;

import core.commands.delivery.PickupOrderCommand;
import core.commands.payment.TransferMoneyCommand;
import core.commands.restaurant.CreateOrderCommand;
import core.models.Address;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import restaurant.entities.Order;
import restaurant.entities.OrderState;
import restaurant.respository.OrderRepository;
import restaurant.util.RestaurantUtil;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Random;

@Component
public class CommandProducer {
    @Value(value = "${kafka.payment.command.topic}")
    private String paymentCommandTopic;

    @Value(value = "${kafka.delivery.command.topic}")
    private String deliveryCommandTopic;

    @Value(value = "${kafka.restaurant.command.topic}")
    private String restaurantCommandTopic;

    @Qualifier("kafkaTemplate")
    @Autowired
    private KafkaTemplate<Long, Object> kafkaTemplate;

    @Autowired
    private OrderRepository orderRepository;

    private static final Logger logger = LoggerFactory.getLogger(CommandProducer.class);

    public void publishPickupOrderCommand(long orderId,long customerId,long restaurantId) {
        PickupOrderCommand pickupOrderCommand = new PickupOrderCommand();
        Optional<Order> optionalOrder= orderRepository.findById(orderId);
        if(optionalOrder.isPresent()){
            Order order = optionalOrder.get();
            Address deliveryAddress = RestaurantUtil.convertAddressEntityToAddressModel(order.getAddress());
            pickupOrderCommand.setOrderId(orderId);
            pickupOrderCommand.setCustomerId(customerId);
            pickupOrderCommand.setRestaurantId(restaurantId);
            pickupOrderCommand.setDeliveryAddress(deliveryAddress);
            kafkaTemplate.send(deliveryCommandTopic,
                    pickupOrderCommand.getOrderId(), pickupOrderCommand);
            kafkaTemplate.flush();
            logger.info("PickupOrderCommand is published for order id: " +pickupOrderCommand.getOrderId());
        }

    }


    public void publishCreateOrderCommand(CreateOrderCommand createOrderCommand){
        kafkaTemplate.send(restaurantCommandTopic, createOrderCommand);
        kafkaTemplate.flush();
    }

    public void publishTransferMoneyCommand(TransferMoneyCommand transferMoneyCommand){
        kafkaTemplate.send(paymentCommandTopic, transferMoneyCommand);
        kafkaTemplate.flush();
    }
}
