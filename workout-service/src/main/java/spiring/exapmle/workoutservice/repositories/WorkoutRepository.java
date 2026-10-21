package spiring.exapmle.workoutservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import spiring.exapmle.workoutservice.models.Workout;
import java.time.LocalDateTime;
import java.util.List;

public interface WorkoutRepository extends JpaRepository<Workout, Long> {
    List<Workout> findByIdTrainer(Long idTrainer);
    List<Workout> findByTrainingProgram_IdTrainingProgram(Long idProgram);
    List<Workout> findByStartDatetimeBetween(LocalDateTime from, LocalDateTime to);
    List<Workout> findByStatus(String status);
    List<Workout> findByIdTrainerAndStartDatetimeBetween(Long idTrainer, LocalDateTime from, LocalDateTime to);
}