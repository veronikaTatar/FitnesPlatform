package spiring.exapmle.analyticsservice.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Instant;

@Entity
@Table(name = "processed_booking_event")
@Getter
@Setter
@NoArgsConstructor
public class ProcessedBookingEvent {
    @Id
    @Column(name = "event_id", length = 36)
    private String eventId;
    @Column(name = "booking_id", nullable = false)
    private Long bookingId;
    @Column(name = "client_id", nullable = false)
    private Long clientId;
    @Column(name = "workout_id", nullable = false)
    private Long workoutId;
    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;
}
