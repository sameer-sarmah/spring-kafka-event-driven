package restaurant.command.handler;

import core.commands.handler.IRestaurantCommandHandler;
import core.commands.payment.CreateAccountCommand;
import core.commands.restaurant.CreateCustomerCommand;
import core.commands.restaurant.IRestaurantCommand;
import core.events.restaurant.CustomerCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import restaurant.command.publisher.CommandProducer;
import restaurant.entities.Customer;
import restaurant.respository.CustomerRepository;
import restaurant.util.RestaurantUtil;

import javax.transaction.Transactional;

@Component
public class CreateCustomerCommandHandler implements IRestaurantCommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(CreateCustomerCommandHandler.class);

    @Value(value = "${kafka.restaurant.event.topic}")
    private String restaurantEventTopic;

    @Qualifier("kafkaTemplate")
    @Autowired
    private KafkaTemplate<Long, Object> kafkaTemplate;

    @Autowired
    private CustomerRepository customerRepository;

    @Override
    public boolean canHandle(IRestaurantCommand restaurantCommand) {
        return restaurantCommand instanceof CreateCustomerCommand;
    }

    @Transactional
    @Override
    public void handle(IRestaurantCommand restaurantCommand) {
        if (restaurantCommand instanceof CreateCustomerCommand) {
            CreateCustomerCommand createCustomerCommand = (CreateCustomerCommand)restaurantCommand;
            createCustomerCommand.getCustomer();
            Customer customer = RestaurantUtil.convertCustomerModelToAddressCustomer(createCustomerCommand.getCustomer());
            customer = customerRepository.saveAndFlush(customer);
            publishCustomerCreatedEvent(createCustomerCommand,customer.getId());
            logger.info("published CustomerCreatedEvent");
        }
    }

    private void publishCustomerCreatedEvent(CreateCustomerCommand createCustomerCommand,Long customerId){
        CustomerCreatedEvent customerCreatedEvent = new CustomerCreatedEvent();
        customerCreatedEvent.setCustomer(createCustomerCommand.getCustomer());
        customerCreatedEvent.setCustomerId(customerId);
        kafkaTemplate.send(restaurantEventTopic,customerCreatedEvent);
    }
}
