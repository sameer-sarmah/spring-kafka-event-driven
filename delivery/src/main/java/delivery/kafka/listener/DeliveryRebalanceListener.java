package delivery.kafka.listener;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.listener.ConsumerAwareRebalanceListener;

import java.util.Collection;

public class DeliveryRebalanceListener implements ConsumerAwareRebalanceListener {
    private static final Logger logger = LoggerFactory.getLogger(DeliveryRebalanceListener.class);
    @Override
    public void onPartitionsRevokedBeforeCommit(Consumer<?,?> consumer, Collection<TopicPartition> partitions) {

    }

    @Override
    public void onPartitionsAssigned(Consumer<?, ?> consumer, Collection<TopicPartition> partitions) {

    }
}
