package delivery;

import delivery.entities.DeliveryAgent;
import delivery.repository.DeliveryAgentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ApplicationListener {

    @Autowired
    private DeliveryAgentRepository deliveryAgentRepository;

    @EventListener
    public void populateDeliveryAgents(ContextStartedEvent ctxStartEvt){
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
