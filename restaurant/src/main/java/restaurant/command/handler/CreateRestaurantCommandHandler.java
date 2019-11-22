package restaurant.command.handler;

import core.commands.handler.IRestaurantCommandHandler;
import core.commands.restaurant.CreateRestaurantCommand;
import core.commands.restaurant.IRestaurantCommand;
import core.events.restaurant.RestaurantCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import restaurant.entities.Restaurant;
import restaurant.respository.RestaurantRepository;
import restaurant.util.RestaurantUtil;

public class CreateRestaurantCommandHandler implements IRestaurantCommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(CreateRestaurantCommandHandler.class);

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Value(value = "${kafka.restaurant.event.topic}")
    private String restaurantEventTopic;

    @Qualifier("kafkaTemplate")
    @Autowired
    private KafkaTemplate<Long, Object> kafkaTemplate;

    @Override
    public boolean canHandle(IRestaurantCommand restaurantCommand) {
        return restaurantCommand instanceof CreateRestaurantCommand;
    }

    @Override
    public void handle(IRestaurantCommand restaurantCommand) {
        if(restaurantCommand instanceof CreateRestaurantCommand){
            CreateRestaurantCommand createRestaurantCommand = (CreateRestaurantCommand)restaurantCommand;
            Restaurant restaurant = RestaurantUtil.convertRestaurantModelToRestaurantEntity(createRestaurantCommand.getRestaurant());
            restaurant = restaurantRepository.saveAndFlush(restaurant);
            publishRestaurantCreatedEvent(createRestaurantCommand,restaurant.getId());
            logger.info("published CreateRestaurantCommand");
        }
    }

    public void publishRestaurantCreatedEvent(CreateRestaurantCommand createRestaurantCommand,Long restaurantId){
        RestaurantCreatedEvent restaurantCreatedEvent = new RestaurantCreatedEvent();
        restaurantCreatedEvent.setRestaurant(createRestaurantCommand.getRestaurant());
        restaurantCreatedEvent.setRestaurantId(restaurantId);
        kafkaTemplate.send(restaurantEventTopic,restaurantCreatedEvent);
    }
}
