package spiring.exapmle.analyticsservice.events;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConsumerConfiguration {
    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerConfiguration.class);
    public static final String BOOKING_CREATED_DLT = "fitness.booking.created.DLT";

    @Bean
    public NewTopic bookingCreatedDeadLetterTopic() {
        return TopicBuilder.name(BOOKING_CREATED_DLT).partitions(1).replicas(1).build();
    }

    @Bean
    public DefaultErrorHandler bookingEventErrorHandler(KafkaTemplate<Object, Object> kafkaTemplate) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, exception) -> {
                    log.error("Booking event {} from topic {} failed after retries; forwarding to {}",
                            record.key(), record.topic(), BOOKING_CREATED_DLT, exception);
                    return new TopicPartition(BOOKING_CREATED_DLT, 0);
                });
        recoverer.setFailIfSendResultIsError(true);
        return new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 2L));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<Object, Object> kafkaListenerContainerFactory(
            ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
            ConsumerFactory<Object, Object> consumerFactory,
            DefaultErrorHandler bookingEventErrorHandler) {
        ConcurrentKafkaListenerContainerFactory<Object, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        configurer.configure(factory, consumerFactory);
        factory.setCommonErrorHandler(bookingEventErrorHandler);
        return factory;
    }
}
