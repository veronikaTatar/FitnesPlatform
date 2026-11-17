package spiring.exapmle.workoutservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import spiring.exapmle.workoutservice.models.Workout;
import java.time.LocalDateTime;
import java.util.List;

public interface WorkoutRepository extends JpaRepository<Workout, Long> {
    List<Workout> findByIdTrainer(Long idTrainer);
    List<Workout> findByTrainingProgram_IdTrainingProgram(Long idProgram);
    boolean existsByTrainingProgram_IdTrainingProgram(Long idProgram);
    List<Workout> findByStartDatetimeBetween(LocalDateTime from, LocalDateTime to);
    List<Workout> findByStatus(String status);
    List<Workout> findByIdTrainerAndStartDatetimeBetween(Long idTrainer, LocalDateTime from, LocalDateTime to);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Workout w set w.bookedCount = w.bookedCount + 1 " +
            "where w.idWorkout = :id and w.status = 'PLANNED' " +
            "and w.bookedCount < w.maxCapacity")
    int reserveAvailableSlot(@Param("id") Long id);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Workout w set w.bookedCount = w.bookedCount - 1 " +
            "where w.idWorkout = :id and w.bookedCount > 0")
    int releaseReservedSlot(@Param("id") Long id);
}
