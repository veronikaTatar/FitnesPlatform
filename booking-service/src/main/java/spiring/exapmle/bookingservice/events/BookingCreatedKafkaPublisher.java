package spiring.exapmle.bookingservice.events;

import by.bsuir.fitness.events.BookingCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class BookingCreatedKafkaPublisher {
    private static final Logger log = LoggerFactory.getLogger(BookingCreatedKafkaPublisher.class);
    private final KafkaTemplate<String, BookingCreatedEvent> kafkaTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishAfterCommit(BookingCreatedEvent event) {
        kafkaTemplate.send(BookingKafkaConfiguration.BOOKING_CREATED_TOPIC,
                        event.eventId(), event)
                .whenComplete((result, error) -> {
                    if (error != null) {
                        log.error("Failed to publish booking event {}", event.eventId(), error);
                    } else {
                        log.info("Published booking event {} to partition {}",
                                event.eventId(), result.getRecordMetadata().partition());
                    }
                });
    }
}
