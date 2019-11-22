package delivery.handler.event.handler;

import core.events.delivery.IDeliveryEvent;
import core.events.delivery.PendingDeliveryProcessedEvent;
import core.events.handler.IDeliveryEventHandler;
import org.springframework.stereotype.Component;

@Component
public class PendingDeliveryProcessedEventHandler implements IDeliveryEventHandler {
    @Override
    public boolean canHandle(IDeliveryEvent deliveryEvent) {
        return deliveryEvent instanceof PendingDeliveryProcessedEvent;
    }

    @Override
    public void handle(IDeliveryEvent deliveryEvent) {
        if(deliveryEvent instanceof PendingDeliveryProcessedEvent){
            PendingDeliveryProcessedEvent pendingDeliveryProcessedEvent = (PendingDeliveryProcessedEvent)deliveryEvent;

        }
    }
}
