package delivery;

import delivery.config.DeliveryKafkaConfig;
import delivery.config.DeliverySQLConfig;
import delivery.entities.DeliveryAgent;
import delivery.publisher.CommandProducer;
import delivery.repository.DeliveryAgentRepository;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.postgresql.Driver;
//@SpringBootApplication
//
//@Import({DeliveryKafkaConfig.class, DeliverySQLConfig.class})
public class DeliveryApp {
    public static void main(String[] args) throws InterruptedException {
        ApplicationContext ctx = new AnnotationConfigApplicationContext(DeliveryKafkaConfig.class,DeliverySQLConfig.class);
        DeliveryAgentRepository deliveryAgentRepository = ctx.getBean(DeliveryAgentRepository.class);
        populateDeliveryAgents(deliveryAgentRepository);
        CommandProducer commandProducer = ctx.getBean(CommandProducer.class);
        //commandProducer.publishPickupOrderCommand();
        Thread.sleep(1000000);
    }

    private static void populateDeliveryAgents(DeliveryAgentRepository deliveryAgentRepository){
        if(deliveryAgentRepository.findAll().isEmpty()){
            DeliveryAgent manoj= new DeliveryAgent();
            manoj.setAvailable(true);
            manoj.setContactName("Manoj");
            manoj.setPhone("2134");

            DeliveryAgent rajesh= new DeliveryAgent();
            rajesh.setAvailable(true);
            rajesh.setContactName("Rajesh");
            rajesh.setPhone("2134");
            deliveryAgentRepository.save(manoj);
            deliveryAgentRepository.save(rajesh);
            deliveryAgentRepository.flush();
        }
    }
}
