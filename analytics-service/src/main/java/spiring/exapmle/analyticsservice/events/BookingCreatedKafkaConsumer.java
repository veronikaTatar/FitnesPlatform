package spiring.exapmle.analyticsservice.events;

import by.bsuir.fitness.events.BookingCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import spiring.exapmle.analyticsservice.models.ProcessedBookingEvent;
import spiring.exapmle.analyticsservice.repositories.ProcessedBookingEventRepository;

@Component
@RequiredArgsConstructor
public class BookingCreatedKafkaConsumer {
    private final ProcessedBookingEventRepository repository;

    @KafkaListener(topics = "fitness.booking.created", groupId = "analytics-service")
    @Transactional
    public void consume(BookingCreatedEvent event) {
        if (repository.existsById(event.eventId())) {
            return;
        }
        ProcessedBookingEvent processed = new ProcessedBookingEvent();
        processed.setEventId(event.eventId());
        processed.setBookingId(event.bookingId());
        processed.setClientId(event.clientId());
        processed.setWorkoutId(event.workoutId());
        processed.setOccurredAt(event.occurredAt());
        repository.save(processed);
    }
}
