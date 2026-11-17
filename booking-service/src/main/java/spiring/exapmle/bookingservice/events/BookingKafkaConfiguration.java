package spiring.exapmle.bookingservice.events;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class BookingKafkaConfiguration {
    public static final String BOOKING_CREATED_TOPIC = "fitness.booking.created";

    @Bean
    public NewTopic bookingCreatedTopic() {
        return TopicBuilder.name(BOOKING_CREATED_TOPIC).partitions(1).replicas(1).build();
    }
}
