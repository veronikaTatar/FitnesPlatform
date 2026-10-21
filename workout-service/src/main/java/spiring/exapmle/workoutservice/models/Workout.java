package spiring.exapmle.workoutservice.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "workout")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Workout {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idWorkout")
    private Long idWorkout;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idTrainingProgram", nullable = false)
    private TrainingProgram trainingProgram;

    /** Логическая ссылка на user_db.trainer.idTrainer (без FK) */
    @Column(name = "idTrainer", nullable = false)
    private Long idTrainer;

    @Column(name = "start_datetime", nullable = false)
    private LocalDateTime startDatetime;

    @Column(name = "duration", nullable = false)
    private Integer duration;

    @Column(name = "status", nullable = false, length = 50)
    private String status; // PLANNED | CANCELLED | COMPLETED

    @Column(name = "max_capacity", nullable = false)
    private Integer maxCapacity;
}