package delivery.config;

import core.deserailizer.DeliveryCommandDeserializer;
import core.deserailizer.DeliveryEventDeserializer;
import core.serializer.KafkaJsonSerializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.LongSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@EnableScheduling
@PropertySource("kafka.properties")
@Configuration
@ComponentScan(basePackages = {"core", "delivery"})
public class DeliveryKafkaConfig {
    @Value(value = "${kafka.bootstrap-server}")
    private String bootstrapAddress;

    @Value(value = "${kafka.command.topic}")
    private String commandTopic;

    @Value(value = "${kafka.event.topic}")
    private String eventTopic;

    @Value(value = "${kafka.event.consumer.group}")
    private String eventConsumerGroup;

    @Value(value = "${kafka.command.consumer.group}")
    private String commandConsumerGroup;

    @Autowired
    private KafkaJsonSerializer kafkaJsonSerializer;

    @Autowired
    private DeliveryCommandDeserializer deliveryCommandDeserializer;

    @Autowired
    private DeliveryEventDeserializer deliveryEventDeserializer;

    @Bean
    public ProducerFactory<Long, Object> commandProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaJsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public ProducerFactory<Long, Object> eventProducerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaJsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<Long, Object> commandKafkaTemplate() {
        return new KafkaTemplate<>(commandProducerFactory());
    }

    @Bean
    public KafkaTemplate<Long, Object> eventKafkaTemplate() {
        return new KafkaTemplate<>(eventProducerFactory());
    }

    @Bean
    public ConsumerFactory<Long, Object> commandConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, commandConsumerGroup);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, "100000");
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 100000);
        props.put(ConsumerConfig.CONNECTIONS_MAX_IDLE_MS_CONFIG, 100000);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, DeliveryCommandDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConsumerFactory<Long, Object> eventConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, eventConsumerGroup);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, "100000");
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 100000);
        props.put(ConsumerConfig.CONNECTIONS_MAX_IDLE_MS_CONFIG, 100000);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, DeliveryEventDeserializer.class);
        return new DefaultKafkaConsumerFactory<>(props);
    }

//    @Bean
//    @DependsOn("commandConsumerFactory")
//    public Consumer<Long, Object> commandConsumer(@Qualifier("commandConsumerFactory") ConsumerFactory<Long, Object> consumerFactory) {
//        return consumerFactory.createConsumer(commandConsumerGroup, "-CommandConsumer");
//    }
//
//    @Bean
//    @DependsOn("eventConsumerFactory")
//    public Consumer<Long, Object> eventConsumer(@Qualifier("eventConsumerFactory") ConsumerFactory<Long, Object> consumerFactory) {
//        return consumerFactory.createConsumer(eventConsumerGroup, "-EventConsumer");
//    }

//    @Bean
//    @DependsOn("commandConsumer")
//    public RebalanceListener commandRebalanceListener(@Qualifier("commandConsumer") Consumer<Long, Object> commandConsumer) {
//        return new RebalanceListener(commandConsumer);
//    }
//
//    @Bean
//    @DependsOn("eventConsumer")
//    public RebalanceListener eventRebalanceListener(@Qualifier("eventConsumer") Consumer<Long, Object> eventConsumer) {
//        return new RebalanceListener(eventConsumer);
//    }

//    @Bean
//    @DependsOn({"commandConsumerFactory","commandRebalanceListener"})
//    public ConcurrentKafkaListenerContainerFactory<Long, Object> kafkaCommandListenerContainerFactory(@Qualifier("commandConsumerFactory") ConsumerFactory<Long, Object> consumerFactory, @Qualifier("commandRebalanceListener") RebalanceListener rebalanceListener                        ) {
//        ConcurrentKafkaListenerContainerFactory<Long, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
//        factory.setConsumerFactory(consumerFactory);
//        factory.setConcurrency(1);
//        factory.getContainerProperties().setConsumerRebalanceListener(rebalanceListener);
//        return factory;
//    }
//
//    @Bean
//    @DependsOn({"eventConsumerFactory","eventRebalanceListener"})
//    public ConcurrentKafkaListenerContainerFactory<Long, Object> kafkaEventListenerContainerFactory(@Qualifier("eventConsumerFactory") ConsumerFactory<Long, Object> consumerFactory, @Qualifier("eventRebalanceListener") RebalanceListener rebalanceListener                        ) {
//        ConcurrentKafkaListenerContainerFactory<Long, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
//        factory.setConsumerFactory(consumerFactory);
//        factory.setConcurrency(1);
//        factory.getContainerProperties().setConsumerRebalanceListener(rebalanceListener);
//        return factory;
//    }


    @Bean
    @DependsOn({"commandConsumerFactory"})
    public ConcurrentKafkaListenerContainerFactory<Long, Object> kafkaCommandListenerContainerFactory(@Qualifier("commandConsumerFactory") ConsumerFactory<Long, Object> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<Long, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setConcurrency(1);
        return factory;
    }

    @Bean
    @DependsOn({"eventConsumerFactory"})
    public ConcurrentKafkaListenerContainerFactory<Long, Object> kafkaEventListenerContainerFactory(@Qualifier("eventConsumerFactory") ConsumerFactory<Long, Object> consumerFactory ) {
        ConcurrentKafkaListenerContainerFactory<Long, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setConcurrency(1);
        return factory;
    }
}
