package restaurant.command.handler;

import core.commands.handler.IPaymentCommandHandler;
import core.commands.handler.IRestaurantCommandHandler;
import core.commands.payment.IPaymentCommand;
import core.commands.restaurant.IRestaurantCommand;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RestaurantCommandHandler {
    @Autowired
    List<IRestaurantCommandHandler> restaurantCommandHandlers;

    @KafkaListener(topics = "#{'${kafka.restaurant.command.topic}'}",
            groupId = "#{'${kafka.command.consumer.group}'}" , containerFactory = "kafkaRestaurantCommandListenerContainerFactory")
    public void handle(@Payload ConsumerRecord message, Consumer<Long,Object> consumer){
        if(message.value() != null && message.value() instanceof IRestaurantCommand){
            IRestaurantCommand event = (IRestaurantCommand)message.value();
            for(IRestaurantCommandHandler commandHandler : restaurantCommandHandlers){
                if(commandHandler.canHandle(event)){
                    commandHandler.handle(event);
                    consumer.commitSync();
                }
            }
        }

    }
}
