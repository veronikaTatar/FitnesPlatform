package spiring.exapmle.workoutservice.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "trainingprogram")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class TrainingProgram {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idTrainingProgram")
    private Long idTrainingProgram;

    /** Логическая ссылка на user_db.trainer.idTrainer (без FK) */
    @Column(name = "idTrainer", nullable = false)
    private Long idTrainer;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "type", length = 50)
    private String type;

    @Column(name = "difficulty_level", length = 50)
    private String difficultyLevel;

    @Column(name = "duration", nullable = false)
    private Integer duration;
}