package restaurant.command.handler;

import core.commands.handler.IRestaurantCommandHandler;
import core.commands.payment.TransferMoneyCommand;
import core.commands.restaurant.CreateOrderCommand;
import core.commands.restaurant.IRestaurantCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import restaurant.command.publisher.CommandProducer;
import restaurant.entities.*;
import restaurant.respository.CustomerRepository;
import restaurant.respository.OrderRepository;
import restaurant.respository.RestaurantRepository;
import restaurant.util.RestaurantUtil;

import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class CreateOrderCommandHandler implements IRestaurantCommandHandler {

    private static final Logger logger = LoggerFactory.getLogger(CreateOrderCommandHandler.class);

    @Autowired
    private CommandProducer commandProducer;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    @Qualifier("entityManagerFactory")
    private EntityManagerFactory entityManagerFactory;

    @Override
    public boolean canHandle(IRestaurantCommand restaurantCommand) {
        return restaurantCommand instanceof CreateOrderCommand;
    }

    //TODO use spring reactor and add wrapper around the repositories to make them async
    @Override
    @Transactional
    public void handle(IRestaurantCommand restaurantCommand) {
        if (restaurantCommand instanceof CreateOrderCommand) {
            CreateOrderCommand createOrderCommand = (CreateOrderCommand)restaurantCommand;
            Optional<Customer> customerOptional = customerRepository.findById(createOrderCommand.getOrder().getCustomerId());
            Optional<Restaurant> restaurantOptional = restaurantRepository.findById(createOrderCommand.getOrder().getRestaurantId());
            if(customerOptional.isPresent() && restaurantOptional.isPresent()){
                Customer customer = customerOptional.get();
                Restaurant restaurant = restaurantOptional.get();
                Order order = new Order();
                order.setAddress(RestaurantUtil.convertAddressModelToAddressEntity(createOrderCommand.getOrder().getAddress()));
                order.setCustomer(customer);
                order.setRestaurant(restaurant);
                Order orderPersisted = orderRepository.save(order);
                List<Long> recipeIds = createOrderCommand.getOrder()
                        .getOrderItems()
                        .stream()
                        .map((orderItem) -> orderItem.getRecipe().getRecipeId())
                        .collect(Collectors.toList());
                TypedQuery<Recipe> query = entityManagerFactory
                        .createEntityManager()
                        .createQuery("select rec from Restaurant res JOIN res.recipes rec WHERE rec.id IN :recipes",Recipe.class);
                query.setParameter("recipes",recipeIds);
                List<Recipe> recipes = query.getResultList();
                Map<Long,Recipe> recipeMap = new HashMap<>();
                recipes.stream().forEach((recipe)->{
                    recipeMap.put(recipe.getId(),recipe);
                });

                List<OrderItem> orderItems = createOrderCommand.getOrder()
                        .getOrderItems()
                        .stream()
                        .map((orderItem) -> {
                            OrderItem orderItemEntity =  RestaurantUtil.convertOrderItemModelToOrderItemEntity(orderItem, orderPersisted);
                            Recipe recipe =recipeMap.get(orderItem.getRecipe().getRecipeId());
                            orderItemEntity.setRecipe(recipe);
                            return orderItemEntity;
                        })
                        .collect(Collectors.toList());

                orderItems.stream().forEach(orderItem -> orderItem.setOrder(orderPersisted));
                orderPersisted.setOrderItems(orderItems);
                orderPersisted.setOrderStatus(OrderState.ORDER_CREATED);
                orderRepository.save(orderPersisted);
                orderRepository.flush();
                TransferMoneyCommand transferMoneyCommand =  new TransferMoneyCommand();
                transferMoneyCommand.setAmount(createOrderCommand.getAmount());
                transferMoneyCommand.setTo(createOrderCommand.getToAccountId());
                transferMoneyCommand.setFrom(createOrderCommand.getFromAccountId());
                transferMoneyCommand.setOrderId(orderPersisted.getId());
                logger.info("Order persisted with status ORDER_CREATED for order id: "+orderPersisted.getId());
                logger.info("TransferMoneyCommand is pusblished for order id: "+orderPersisted.getId());
            }
        }
    }
}
