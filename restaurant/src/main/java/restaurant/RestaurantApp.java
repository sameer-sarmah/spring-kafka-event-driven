package restaurant;


import core.commands.restaurant.CreateOrderCommand;
import core.models.Order;
import core.models.OrderItem;
import core.models.Recipe;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import restaurant.command.publisher.CommandProducer;
import restaurant.config.RestaurantKafkaConfig;
import restaurant.config.RestaurantSQLConfig;
import restaurant.entities.Address;
import restaurant.entities.Customer;
import restaurant.entities.Restaurant;
import restaurant.respository.CustomerRepository;
import restaurant.respository.RestaurantRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class RestaurantApp {
    public static void main(String[] args) throws InterruptedException {

        ApplicationContext ctx = new AnnotationConfigApplicationContext(RestaurantKafkaConfig.class, RestaurantSQLConfig.class);
        RestaurantRepository restaurantRepository = ctx.getBean(RestaurantRepository.class);
        CustomerRepository customerRepository = ctx.getBean(CustomerRepository.class);

        //customerRepository.saveAndFlush(createCustomer());
        //restaurantRepository.saveAndFlush(createRestaurant());
        CommandProducer commandProducer = ctx.getBean(CommandProducer.class);
        commandProducer.publishCreateOrderCommand(buildCreateOrderCommand());

        Thread.sleep(1000000);
    }

    private static Customer createCustomer() {
        Customer customer = new Customer();
        customer.setEmail("sam@gmail.com");
        Address address = new Address();
        address.setAddress("DSR Spring beauty Apt,Brookfield");
        address.setCity("Bangalore");
        address.setCountry("India");
        address.setPhone("1234");
        address.setState("Karnataka");
        address.setZip("560037");
        customer.setAddress(address);
        customer.setName("Sameer");
        return customer;
    }

    private static Restaurant createRestaurant() {
        Address address = new Address();
        address.setAddress("Moriz Restaurant,Brookfield");
        address.setCity("Bangalore");
        address.setCountry("India");
        address.setPhone("458878");
        address.setState("Karnataka");
        address.setZip("560037");
        Restaurant restaurant = new Restaurant();
        restaurant.setAddress(address);
        restaurant.setName("Moriz Restaurant");
        restaurant.setEmail("moriz.restaurant@gmail.com");
        return restaurant;
    }

    private static Order createOrder() {
        Order order = new Order();
        List<OrderItem> orderItems = new ArrayList<>();
        Recipe recipe = new Recipe();
        recipe.setUnitPrice(50);
        recipe.setDescription("Grilled chicked");
        recipe.setName("Grilled chicked");
        OrderItem orderItem = new OrderItem();
        orderItem.setQuantity(1);
        orderItem.setRecipe(recipe);
        orderItems.add(orderItem);
        order.setOrderItems(orderItems);
        core.models.Address address = new core.models.Address();
        address.setAddress("DSR Spring beauty Apt,Brookfield");
        address.setCity("Bangalore");
        address.setCountry("India");
        address.setPhone("1234");
        address.setState("Karnataka");
        address.setZip("560037");
        order.setAddress(address);
        order.setRestaurantId(1L);
        order.setCustomerId(1L);
        return order;
    }

    private static CreateOrderCommand buildCreateOrderCommand() {
        CreateOrderCommand createOrderCommand = new CreateOrderCommand();
        createOrderCommand.setOrder(createOrder());
        createOrderCommand.setAmount(new BigDecimal(50));
        createOrderCommand.setFromAccountId(3L);
        createOrderCommand.setToAccountId(4L);
        return createOrderCommand;
    }
}
