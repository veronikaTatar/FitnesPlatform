package spiring.exapmle.workoutservice.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import spiring.exapmle.workoutservice.models.Progress;
import spiring.exapmle.workoutservice.models.TrainingProgram;
import spiring.exapmle.workoutservice.models.Workout;
import spiring.exapmle.workoutservice.repositories.ProgressRepository;
import spiring.exapmle.workoutservice.repositories.TrainingProgramRepository;
import spiring.exapmle.workoutservice.repositories.WorkoutRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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
        if (idTrainer != null) return programRepository.findByIdTrainer(idTrainer);
        if (type != null && difficultyLevel != null)
            return programRepository.findByTypeAndDifficultyLevel(type, difficultyLevel);
        if (type != null) return programRepository.findByType(type);
        if (difficultyLevel != null) return programRepository.findByDifficultyLevel(difficultyLevel);
        return programRepository.findAll();
    }

    @Override
    public Workout getWorkoutById(Long id) {
        return workoutRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Workout not found: " + id));
    }

    @Override
    public List<Workout> getWorkouts(Long idTrainer, Long idProgram, LocalDateTime from, LocalDateTime to, String status) {
        if (idTrainer != null && from != null && to != null)
            return workoutRepository.findByIdTrainerAndStartDatetimeBetween(idTrainer, from, to);
        if (idTrainer != null) return workoutRepository.findByIdTrainer(idTrainer);
        if (idProgram != null) return workoutRepository.findByTrainingProgram_IdTrainingProgram(idProgram);
        if (from != null && to != null)
            return workoutRepository.findByStartDatetimeBetween(from, to);
        if (status != null) return workoutRepository.findByStatus(status);
        return workoutRepository.findAll();
    }

    @Override
    public List<Workout> getWorkoutsByTrainer(Long idTrainer) {
        return workoutRepository.findByIdTrainer(idTrainer);
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