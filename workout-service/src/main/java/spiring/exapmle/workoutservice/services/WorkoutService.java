package spiring.exapmle.workoutservice.services;

import spiring.exapmle.workoutservice.models.Progress;
import spiring.exapmle.workoutservice.models.TrainingProgram;
import spiring.exapmle.workoutservice.models.Workout;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface WorkoutService {
    TrainingProgram getProgramById(Long id);
    List<TrainingProgram> getPrograms(String type, String difficultyLevel, Long idTrainer);
    TrainingProgram createProgram(TrainingProgram program);
    TrainingProgram updateProgram(Long id, TrainingProgram patch);
    void archiveProgram(Long id);

    Workout getWorkoutById(Long id);
    List<Workout> getWorkouts(Long idTrainer, Long idProgram, LocalDateTime from, LocalDateTime to, String status);
    List<Workout> getWorkoutsByTrainer(Long idTrainer);
    Workout createWorkout(Long programId, java.time.LocalDateTime start, Integer duration, Integer capacity);
    Workout updateWorkout(Long id, java.time.LocalDateTime start, Integer duration, Integer capacity);
    Workout changeWorkoutStatus(Long id, String status);

    boolean reserveSlot(Long idWorkout);
    boolean releaseSlot(Long idWorkout);

    Progress getProgressById(Long id);
    Progress logProgress(Long clientId, Long bookingId, java.math.BigDecimal value, String comment);
    List<Progress> getProgressByClient(Long idClient);
    List<Progress> getProgressByClientAndPeriod(Long idClient, LocalDate from, LocalDate to);
    List<Progress> getProgressByBooking(Long idBooking);
}
