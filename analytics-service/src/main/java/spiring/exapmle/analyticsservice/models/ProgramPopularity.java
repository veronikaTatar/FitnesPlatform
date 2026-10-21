package spiring.exapmle.analyticsservice.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "programpopularity")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ProgramPopularity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idStat")
    private Long idStat;

    /** Логическая ссылка на workout_db.trainingprogram.idTrainingProgram */
    @Column(name = "idTrainingProgram", nullable = false)
    private Long idTrainingProgram;

    @Column(name = "booking_count", nullable = false)
    private Integer bookingCount;

    @Column(name = "period", nullable = false, length = 20)
    private String period; // "week" | "month"
}