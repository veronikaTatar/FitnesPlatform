package spiring.exapmle.workoutservice.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spiring.exapmle.workoutservice.models.Progress;
import spiring.exapmle.workoutservice.models.TrainingProgram;
import spiring.exapmle.workoutservice.models.Workout;
import spiring.exapmle.workoutservice.repositories.ProgressRepository;
import spiring.exapmle.workoutservice.repositories.TrainingProgramRepository;
import spiring.exapmle.workoutservice.repositories.WorkoutRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkoutServiceImpl implements WorkoutService {

    private final TrainingProgramRepository programRepository;
    private final WorkoutRepository workoutRepository;
    private final ProgressRepository progressRepository;

    @Override
    public TrainingProgram getProgramById(Long id) {
        return programRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("TrainingProgram not found: " + id));
    }

    @Override
    public List<TrainingProgram> getPrograms(String type, String difficultyLevel, Long idTrainer) {
        return programRepository.findAll().stream()
                .filter(p -> type == null || type.equals(p.getType()))
                .filter(p -> difficultyLevel == null
                        || difficultyLevel.equals(p.getDifficultyLevel()))
                .filter(p -> idTrainer == null || idTrainer.equals(p.getIdTrainer()))
                .collect(Collectors.toList());
    }

    @Override
    public Workout getWorkoutById(Long id) {
        return workoutRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Workout not found: " + id));
    }

    @Override
    public List<Workout> getWorkouts(Long idTrainer, Long idProgram, LocalDateTime from, LocalDateTime to, String status) {
        return workoutRepository.findAll().stream()
                .filter(w -> idTrainer == null || idTrainer.equals(w.getIdTrainer()))
                .filter(w -> idProgram == null || idProgram.equals(
                        w.getTrainingProgram().getIdTrainingProgram()))
                .filter(w -> from == null || !w.getStartDatetime().isBefore(from))
                .filter(w -> to == null || !w.getStartDatetime().isAfter(to))
                .filter(w -> status == null || status.equals(w.getStatus()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Workout> getWorkoutsByTrainer(Long idTrainer) {
        return workoutRepository.findByIdTrainer(idTrainer);
    }

    @Override
    @Transactional
    public boolean reserveSlot(Long idWorkout) {
        return workoutRepository.reserveAvailableSlot(idWorkout) == 1;
    }

    @Override
    @Transactional
    public boolean releaseSlot(Long idWorkout) {
        return workoutRepository.releaseReservedSlot(idWorkout) == 1;
    }

    @Override
    public Progress getProgressById(Long id) {
        return progressRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Progress not found: " + id));
    }

    @Override
    public List<Progress> getProgressByClient(Long idClient) {
        return progressRepository.findByIdClientOrderByDateDesc(idClient);
    }

    @Override
    public List<Progress> getProgressByClientAndPeriod(Long idClient, LocalDate from, LocalDate to) {
        return progressRepository.findByIdClientAndDateBetween(idClient, from, to);
    }

    @Override
    public List<Progress> getProgressByBooking(Long idBooking) {
        return progressRepository.findByIdBooking(idBooking);
    }
}
