package spiring.exapmle.workoutservice.models;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "progress")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Progress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idProgress")
    private Long idProgress;

    /** Логические ссылки на user_db.client.idClient и booking_db.booking.idBooking */
    @Column(name = "idClient", nullable = false)
    private Long idClient;

    @Column(name = "idBooking", nullable = false)
    private Long idBooking;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "value", precision = 10, scale = 2)
    private BigDecimal value;

    @Column(name = "trainer_comment", columnDefinition = "TEXT")
    private String trainerComment;
}