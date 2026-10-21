package spiring.exapmle.apigateway.client.mock;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import spiring.exapmle.apigateway.client.WorkoutServiceClient;
import spiring.exapmle.apigateway.dto.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;


@Service
@Primary
public class MockWorkoutServiceClient implements WorkoutServiceClient {

    private final List<TrainingProgramDto> programs = new ArrayList<>();
    private final List<WorkoutDto> workouts = new ArrayList<>();
    private final List<ProgressDto> progress = new ArrayList<>();

    private final AtomicLong programIdCounter = new AtomicLong(100);
    private final AtomicLong workoutIdCounter = new AtomicLong(100);
    private final AtomicLong progressIdCounter = new AtomicLong(100);

    public MockWorkoutServiceClient() {

        // ==== Тренеры (для вложенных ссылок) ====
        TrainerDto trainer1 = trainer(1L, "Йога", 4.8f);
        TrainerDto trainer2 = trainer(2L, "Силовая", 4.6f);

        // ==== Тренировочные программы (3 шт.) ====
        TrainingProgramDto program1 = program(1L, "Йога для начинающих",
                "Йога", "beginner", 60, trainer1);
        TrainingProgramDto program2 = program(2L, "Силовая тренировка",
                "Силовая", "advanced", 90, trainer2);
        TrainingProgramDto program3 = program(3L, "Утренняя растяжка",
                "Йога", "intermediate", 45, trainer1);

        programs.add(program1);
        programs.add(program2);
        programs.add(program3);

        // ==== Занятия (5 шт.) ====
        workouts.add(workout(1L, "2026-10-05T18:00:00", 60,
                "PLANNED", 15, program1, trainer1));
        workouts.add(workout(2L, "2026-10-07T18:00:00", 60,
                "PLANNED", 15, program1, trainer1));
        workouts.add(workout(3L, "2026-10-06T19:00:00", 90,
                "PLANNED", 10, program2, trainer2));
        workouts.add(workout(4L, "2026-10-08T08:00:00", 45,
                "PLANNED", 20, program3, trainer1));
        workouts.add(workout(5L, "2026-10-01T19:00:00", 90,
                "COMPLETED", 10, program2, trainer2));

        // ==== Записи прогресса (2 шт.) ====
        progress.add(progress(1L, "2026-09-25", 72.5f,
                "Хороший прогресс", client(1L)));
        progress.add(progress(2L, "2026-09-30", 70.0f,
                "Отличный результат", client(1L)));
    }

    @Override
    public List<TrainingProgramDto> getPrograms(String type, String difficultyLevel, Long trainerId) {
        return programs.stream()
                .filter(p -> type == null || type.equals(p.getType()))
                .filter(p -> difficultyLevel == null || difficultyLevel.equals(p.getDifficultyLevel()))
                .collect(Collectors.toList());
    }

    @Override
    public TrainingProgramDto getProgram(Long id) {
        return programs.stream()
                .filter(p -> id.equals(p.getIdTrainingProgram()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Program not found: " + id));
    }

    @Override
    public List<WorkoutDto> getWorkouts(Long trainerId, Long programId,
                                        String dateFrom, String dateTo, String status) {
        return workouts.stream()
                .filter(w -> trainerId == null || trainerId.equals(w.getTrainer().getIdTrainer()))
                .filter(w -> programId == null || programId.equals(w.getTrainingProgram().getIdTrainingProgram()))
                .filter(w -> status == null || status.equals(w.getStatus()))
                .collect(Collectors.toList());
    }

    @Override
    public WorkoutDto getWorkout(Long id) {
        return workouts.stream()
                .filter(w -> id.equals(w.getIdWorkout()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Workout not found: " + id));
    }

    @Override
    public List<ProgressDto> getProgressByClient(Long clientId) {
        return progress.stream()
                .filter(p -> clientId.equals(p.getClient() != null ? p.getClient().getIdClient() : null))
                .collect(Collectors.toList());
    }

    @Override
    public List<ProgressDto> getProgressByClientAndPeriod(Long clientId, String dateFrom, String dateTo) {
        return getProgressByClient(clientId);
    }


    @Override
    public TrainingProgramDto createTrainingProgram(CreateTrainingProgramInputDto input) {
        if (input.getName() == null || input.getName().isBlank()) {
            throw new RuntimeException("Program name is required");
        }
        if (input.getDuration() == null || input.getDuration() <= 0) {
            throw new RuntimeException("Duration must be positive");
        }

        TrainingProgramDto p = new TrainingProgramDto();
        p.setIdTrainingProgram(programIdCounter.getAndIncrement());
        p.setName(input.getName());
        p.setDescription(input.getDescription());
        p.setType(input.getType());
        p.setDifficultyLevel(input.getDifficultyLevel());
        p.setDuration(input.getDuration());
        programs.add(p);
        return p;
    }

    @Override
    public TrainingProgramDto updateTrainingProgram(Long id,
                                                    UpdateTrainingProgramInputDto input) {
        TrainingProgramDto p = getProgram(id);
        if (input.getName() != null) p.setName(input.getName());
        if (input.getDescription() != null) p.setDescription(input.getDescription());
        if (input.getType() != null) p.setType(input.getType());
        if (input.getDifficultyLevel() != null)
            p.setDifficultyLevel(input.getDifficultyLevel());
        if (input.getDuration() != null) p.setDuration(input.getDuration());
        return p;
    }

    @Override
    public Boolean archiveTrainingProgram(Long id) {
        TrainingProgramDto p = getProgram(id);
        programs.remove(p);
        return true;
    }

    @Override
    public WorkoutDto createWorkout(CreateWorkoutInputDto input) {
        TrainingProgramDto program = getProgram(input.getIdTrainingProgram());

        WorkoutDto w = new WorkoutDto();
        w.setIdWorkout(workoutIdCounter.getAndIncrement());
        w.setStartDatetime(input.getStartDatetime());
        w.setDuration(input.getDuration());
        w.setMaxCapacity(input.getMaxCapacity());
        w.setStatus("PLANNED");
        w.setTrainingProgram(program);
        w.setTrainer(program.getTrainer());
        workouts.add(w);
        return w;
    }

    @Override
    public WorkoutDto updateWorkout(Long id, UpdateWorkoutInputDto input) {
        WorkoutDto w = getWorkout(id);
        if (input.getStartDatetime() != null) w.setStartDatetime(input.getStartDatetime());
        if (input.getDuration() != null) w.setDuration(input.getDuration());
        if (input.getMaxCapacity() != null) w.setMaxCapacity(input.getMaxCapacity());
        return w;
    }

    @Override
    public WorkoutDto changeWorkoutStatus(Long id, String status) {
        WorkoutDto w = getWorkout(id);
        Set<String> allowed = Set.of("PLANNED", "CANCELLED", "COMPLETED");
        if (!allowed.contains(status)) {
            throw new RuntimeException("Invalid workout status: " + status);
        }
        w.setStatus(status);
        return w;
    }

    @Override
    public ProgressDto logProgress(LogProgressInputDto input) {
        if (input.getIdClient() == null) {
            throw new RuntimeException("Client id is required");
        }
        ProgressDto p = new ProgressDto();
        p.setIdProgress(progressIdCounter.getAndIncrement());
        p.setDate(LocalDate.now().toString());
        p.setValue(input.getValue());
        p.setTrainerComment(input.getTrainerComment());
        // client подставляется из user-service в ЛР6
        progress.add(p);
        return p;
    }


    // ================== Вспомогательные фабрики для заполнения конструктора ==================

    private static TrainerDto trainer(Long id, String spec, Float rating) {
        TrainerDto t = new TrainerDto();
        t.setIdTrainer(id);
        t.setSpecialization(spec);
        t.setRating(rating);
        return t;
    }

    private static TrainingProgramDto program(Long id, String name, String type,
                                              String difficulty, Integer duration,
                                              TrainerDto trainer) {
        TrainingProgramDto p = new TrainingProgramDto();
        p.setIdTrainingProgram(id);
        p.setName(name);
        p.setType(type);
        p.setDifficultyLevel(difficulty);
        p.setDuration(duration);
        p.setTrainer(trainer);
        return p;
    }

    private static WorkoutDto workout(Long id, String start, Integer duration,
                                      String status, Integer maxCapacity,
                                      TrainingProgramDto program, TrainerDto trainer) {
        WorkoutDto w = new WorkoutDto();
        w.setIdWorkout(id);
        w.setStartDatetime(start);
        w.setDuration(duration);
        w.setStatus(status);
        w.setMaxCapacity(maxCapacity);
        w.setTrainingProgram(program);
        w.setTrainer(trainer);
        return w;
    }

    private static ClientDto client(Long id) {
        ClientDto c = new ClientDto();
        c.setIdClient(id);
        return c;
    }

    private static ProgressDto progress(Long id, String date, Float value,
                                        String comment, ClientDto client) {
        ProgressDto p = new ProgressDto();
        p.setIdProgress(id);
        p.setDate(date);
        p.setValue(value);
        p.setTrainerComment(comment);
        p.setClient(client);
        return p;
    }



    //ЛР4 Фильтрация
    @Override
    public List<WorkoutDto> getWorkouts(
            Long trainerId, Long programId,
            String dateFrom, String dateTo, String status,
            String sortBy, String sortOrder) {

        List<WorkoutDto> result = new ArrayList<>(workouts);

        // Фильтр по тренеру
        if (trainerId != null) {
            result = result.stream()
                    .filter(w -> w.getTrainer() != null
                            && trainerId.equals(w.getTrainer().getIdTrainer()))
                    .collect(Collectors.toList());
        }

        // Фильтр по программе
        if (programId != null) {
            result = result.stream()
                    .filter(w -> w.getTrainingProgram() != null
                            && programId.equals(w.getTrainingProgram()
                            .getIdTrainingProgram()))
                    .collect(Collectors.toList());
        }

        // Фильтр по дате (от)
        if (dateFrom != null) {
            result = result.stream()
                    .filter(w -> w.getStartDatetime().compareTo(dateFrom) >= 0)
                    .collect(Collectors.toList());
        }

        // Фильтр по дате (до)
        if (dateTo != null) {
            result = result.stream()
                    .filter(w -> w.getStartDatetime().compareTo(dateTo) <= 0)
                    .collect(Collectors.toList());
        }

        // Фильтр по статусу
        if (status != null) {
            result = result.stream()
                    .filter(w -> status.equals(w.getStatus()))
                    .collect(Collectors.toList());
        }

        // Сортировка
        return sortWorkouts(result, sortBy, sortOrder);
    }
    @Override
    public List<TrainingProgramDto> getPrograms(
            String type, String difficultyLevel, Long trainerId,
            String sortBy, String sortOrder) {

        List<TrainingProgramDto> result = new ArrayList<>(programs);

        if (type != null) {
            result = result.stream()
                    .filter(p -> type.equals(p.getType()))
                    .collect(Collectors.toList());
        }
        if (difficultyLevel != null) {
            result = result.stream()
                    .filter(p -> difficultyLevel.equals(p.getDifficultyLevel()))
                    .collect(Collectors.toList());
        }
        if (trainerId != null) {
            result = result.stream()
                    .filter(p -> p.getTrainer() != null
                            && trainerId.equals(p.getTrainer().getIdTrainer()))
                    .collect(Collectors.toList());
        }

        return sortPrograms(result, sortBy, sortOrder);
    }


//ЛР 4 Сортировка

    private List<WorkoutDto> sortWorkouts(List<WorkoutDto> list,
                                          String sortBy, String sortOrder) {
        if (sortBy == null) return list;

        Comparator<WorkoutDto> comparator = switch (sortBy) {
            case "startDatetime" -> Comparator.comparing(WorkoutDto::getStartDatetime);
            case "duration"      -> Comparator.comparing(WorkoutDto::getDuration);
            case "popularity"    -> Comparator.comparing(
                    w -> countBookingsForWorkout(w.getIdWorkout()));
            case "id"            -> Comparator.comparing(WorkoutDto::getIdWorkout);
            default              -> Comparator.comparing(WorkoutDto::getIdWorkout);
        };

        if ("DESC".equalsIgnoreCase(sortOrder)) {
            comparator = comparator.reversed();
        }

        list.sort(comparator);
        return list;
    }

    private long countBookingsForWorkout(Long workoutId) {
        // В mock-клиенте workouts не знает о bookings,
        // поэтому popularity считается через поле maxCapacity - bookedCount
        // или через отдельный счётчик, поддерживаемый при bookWorkout.
        return 0L; // в ЛР6 popularity придёт из Analytics Service через gRPC
    }
    private List<TrainingProgramDto> sortPrograms(List<TrainingProgramDto> list,
                                                  String sortBy, String sortOrder) {
        if (sortBy == null) return list;

        Comparator<TrainingProgramDto> comparator = switch (sortBy) {
            case "name"       -> Comparator.comparing(TrainingProgramDto::getName);
            case "duration"   -> Comparator.comparing(TrainingProgramDto::getDuration);
            case "type"       -> Comparator.comparing(TrainingProgramDto::getType);
            case "id"         -> Comparator.comparing(TrainingProgramDto::getIdTrainingProgram);
            default           -> Comparator.comparing(TrainingProgramDto::getIdTrainingProgram);
        };

        if ("DESC".equalsIgnoreCase(sortOrder)) {
            comparator = comparator.reversed();
        }

        list.sort(comparator);
        return list;
    }

    //ЛР 4 Реализация операций создания и изменения данных

}