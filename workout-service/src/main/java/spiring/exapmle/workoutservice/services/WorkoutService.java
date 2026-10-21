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

    Workout getWorkoutById(Long id);
    List<Workout> getWorkouts(Long idTrainer, Long idProgram, LocalDateTime from, LocalDateTime to, String status);
    List<Workout> getWorkoutsByTrainer(Long idTrainer);

    Progress getProgressById(Long id);
    List<Progress> getProgressByClient(Long idClient);
    List<Progress> getProgressByClientAndPeriod(Long idClient, LocalDate from, LocalDate to);
    List<Progress> getProgressByBooking(Long idBooking);
}