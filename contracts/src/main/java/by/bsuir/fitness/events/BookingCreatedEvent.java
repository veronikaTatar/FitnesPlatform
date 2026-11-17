package by.bsuir.fitness.events;

import java.time.Instant;

/** Event emitted after a booking has been committed. */
public record BookingCreatedEvent(String eventId, Long bookingId, Long clientId,
                                  Long workoutId, Instant occurredAt) {
}
