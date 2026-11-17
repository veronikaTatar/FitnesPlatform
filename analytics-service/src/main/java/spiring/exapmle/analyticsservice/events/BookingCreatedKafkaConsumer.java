package spiring.exapmle.analyticsservice.events;

import by.bsuir.fitness.events.BookingCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import spiring.exapmle.analyticsservice.models.ProcessedBookingEvent;
import spiring.exapmle.analyticsservice.repositories.ProcessedBookingEventRepository;

@Component
@RequiredArgsConstructor
public class BookingCreatedKafkaConsumer {
    private static final Logger log = LoggerFactory.getLogger(BookingCreatedKafkaConsumer.class);
    private final ProcessedBookingEventRepository repository;

    @KafkaListener(topics = "fitness.booking.created", groupId = "analytics-service")
    @Transactional
    public void consume(BookingCreatedEvent event) {
        if (repository.existsById(event.eventId())) {
            log.info("Ignoring duplicate booking event {}", event.eventId());
            return;
        }
        ProcessedBookingEvent processed = new ProcessedBookingEvent();
        processed.setEventId(event.eventId());
        processed.setBookingId(event.bookingId());
        processed.setClientId(event.clientId());
        processed.setWorkoutId(event.workoutId());
        processed.setOccurredAt(event.occurredAt());
        repository.save(processed);
        log.info("Processed booking event {} for booking {}", event.eventId(), event.bookingId());
    }
}
