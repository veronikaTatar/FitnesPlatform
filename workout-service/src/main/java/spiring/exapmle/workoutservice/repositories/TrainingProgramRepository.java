package spiring.exapmle.workoutservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import spiring.exapmle.workoutservice.models.TrainingProgram;
import java.util.List;

public interface TrainingProgramRepository extends JpaRepository<TrainingProgram, Long> {
    List<TrainingProgram> findByType(String type);
    List<TrainingProgram> findByDifficultyLevel(String difficultyLevel);
    List<TrainingProgram> findByIdTrainer(Long idTrainer);
    List<TrainingProgram> findByTypeAndDifficultyLevel(String type, String difficultyLevel);
}