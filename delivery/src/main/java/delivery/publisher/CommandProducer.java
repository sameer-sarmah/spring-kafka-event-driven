package delivery.publisher;

import core.commands.delivery.PickupOrderCommand;
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

import java.util.Random;

@Component
public class CommandProducer {
    @Value(value = "${kafka.command.topic}")
    private String commandTopic;

    @Qualifier("commandKafkaTemplate")
    @Autowired
    private KafkaTemplate<Long, Object> kafkaTemplate;

    private static final Logger logger = LoggerFactory.getLogger(CommandProducer.class);

    public void publishPickupOrderCommand() {
        PickupOrderCommand pickupOrderCommand = new PickupOrderCommand();
        Random random = new Random();
        pickupOrderCommand.setOrderId(random.nextInt(100000));
        pickupOrderCommand.setCustomerId(random.nextInt(100000));
        pickupOrderCommand.setRestaurantId(random.nextInt(100000));
        Address deliveryAddress = new Address();
        deliveryAddress.setAddress("Brookfield");
        deliveryAddress.setCity("Bangalore");
        deliveryAddress.setCountry("India");
        deliveryAddress.setPhone("1234");
        deliveryAddress.setState("Karnataka");
        deliveryAddress.setZip("560037");
        pickupOrderCommand.setDeliveryAddress(deliveryAddress);

        ListenableFuture<SendResult<Long, Object>> future =  kafkaTemplate.send(commandTopic,
                pickupOrderCommand.getOrderId(), pickupOrderCommand);
        kafkaTemplate.flush();
        logger.info("PickupOrderCommand is published for order id: " +pickupOrderCommand.getOrderId());
    }


}
