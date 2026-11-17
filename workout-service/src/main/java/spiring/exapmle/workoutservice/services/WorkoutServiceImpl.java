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
    @Transactional
    public TrainingProgram createProgram(TrainingProgram program) {
        if (program.getName() == null || program.getName().isBlank()) {
            throw new IllegalArgumentException("Program name is required");
        }
        if (program.getDuration() == null || program.getDuration() <= 0) {
            throw new IllegalArgumentException("Program duration must be positive");
        }
        if (program.getIdTrainer() == null || program.getIdTrainer() <= 0) {
            throw new IllegalArgumentException("Trainer id is required");
        }
        return programRepository.save(program);
    }

    @Override @Transactional
    public TrainingProgram updateProgram(Long id, TrainingProgram patch) {
        TrainingProgram current = getProgramById(id);
        if (patch.getDuration() != null && patch.getDuration() <= 0) {
            throw new IllegalArgumentException("Program duration must be positive");
        }
        if (patch.getName() != null && !patch.getName().isBlank()) current.setName(patch.getName());
        if (patch.getDescription() != null) current.setDescription(patch.getDescription());
        if (patch.getType() != null) current.setType(patch.getType());
        if (patch.getDifficultyLevel() != null) current.setDifficultyLevel(patch.getDifficultyLevel());
        if (patch.getDuration() != null && patch.getDuration() > 0) current.setDuration(patch.getDuration());
        return programRepository.save(current);
    }

    @Override @Transactional
    public void archiveProgram(Long id) {
        if (!programRepository.existsById(id)) throw new RuntimeException("TrainingProgram not found: " + id);
        if (workoutRepository.existsByTrainingProgram_IdTrainingProgram(id))
            throw new IllegalStateException("Cannot archive a program that has scheduled workouts");
        programRepository.deleteById(id);
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
    @Transactional
    public Workout createWorkout(Long programId, LocalDateTime start, Integer duration, Integer capacity) {
        TrainingProgram program = getProgramById(programId);
        if (start == null || duration == null || duration <= 0 || capacity == null || capacity <= 0)
            throw new IllegalArgumentException("Start time, positive duration, and capacity are required");
        Workout workout = new Workout(); workout.setTrainingProgram(program); workout.setIdTrainer(program.getIdTrainer());
        workout.setStartDatetime(start); workout.setDuration(duration); workout.setMaxCapacity(capacity);
        workout.setStatus("PLANNED"); workout.setBookedCount(0); return workoutRepository.save(workout);
    }

    @Override
    @Transactional
    public Workout updateWorkout(Long id, LocalDateTime start, Integer duration, Integer capacity) {
        Workout workout = getWorkoutById(id);
        if (duration != null && duration <= 0) {
            throw new IllegalArgumentException("Workout duration must be positive");
        }
        if (capacity != null && capacity < workout.getBookedCount()) {
            throw new IllegalArgumentException("Capacity cannot be below the current booking count");
        }
        if (start != null) workout.setStartDatetime(start);
        if (duration != null) workout.setDuration(duration);
        if (capacity != null && capacity > 0) workout.setMaxCapacity(capacity);
        return workoutRepository.save(workout);
    }

    @Override @Transactional
    public Workout changeWorkoutStatus(Long id, String status) {
        if (status == null || !List.of("PLANNED", "CANCELLED", "COMPLETED").contains(status)) {
            throw new IllegalArgumentException("Unsupported workout status: " + status);
        }
        Workout workout = getWorkoutById(id);
        workout.setStatus(status);
        return workoutRepository.save(workout);
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
    @Transactional
    public Progress logProgress(Long clientId, Long bookingId, java.math.BigDecimal value, String comment) {
        if (clientId == null || clientId <= 0 || value == null || value.signum() <= 0) {
            throw new IllegalArgumentException("Client id and positive progress value are required");
        }
        Progress progress = new Progress();
        progress.setIdClient(clientId);
        progress.setIdBooking(bookingId);
        progress.setDate(LocalDate.now());
        progress.setValue(value);
        progress.setTrainerComment(comment);
        return progressRepository.save(progress);
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
