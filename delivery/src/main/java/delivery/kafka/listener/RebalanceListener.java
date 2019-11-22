package delivery.kafka.listener;


import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class RebalanceListener implements ConsumerRebalanceListener {

    private static final Logger logger = LoggerFactory.getLogger(RebalanceListener.class);
    private Consumer consumer;
    private Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();

    public RebalanceListener(Consumer consumer) {
        this.consumer = consumer;
    }

    public void addOffset(String topic, int partition, long offset) {

        currentOffsets.put(new TopicPartition(topic, partition), new OffsetAndMetadata(offset, "Commit"));
    }

    public Map<TopicPartition, OffsetAndMetadata> getCurrentOffsets() {
        return currentOffsets;
    }

    public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
        logger.info("Partition Assigned to consumer "+ consumer.toString());
        for (TopicPartition partition : partitions)
            logger.info(partition.partition() + ",");
    }

    public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
        logger.info("Partition revoked from consumer "+ consumer.toString());
        for (TopicPartition partition : partitions)
            logger.info(partition.partition() + ",");


        logger.info("Following Partitions commited ....");
        for (TopicPartition tp : currentOffsets.keySet())
            logger.info(tp.partition()+", ");

        consumer.commitSync(currentOffsets);
        currentOffsets.clear();
    }
}
