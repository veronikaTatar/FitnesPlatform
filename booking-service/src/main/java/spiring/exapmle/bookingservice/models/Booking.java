package spiring.exapmle.bookingservice.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import spiring.exapmle.bookingservice.models.enums.BookingStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "booking")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idBooking")
    private Long idBooking;

    @Column(name = "idClient", nullable = false)
    private Long idClient;

    @Column(name = "idWorkout", nullable = false)
    private Long idWorkout;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)              // ← ДОБАВИТЬ
    @Column(name = "status", nullable = false, length = 50)
    private BookingStatus status;

    @Column(name = "booking_date", nullable = false)
    private LocalDateTime bookingDate;
}