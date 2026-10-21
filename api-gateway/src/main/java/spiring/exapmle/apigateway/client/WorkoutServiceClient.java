package spiring.exapmle.apigateway.client;

import spiring.exapmle.apigateway.dto.*;

import java.util.List;

public interface WorkoutServiceClient {
    List<TrainingProgramDto> getPrograms(String type, String difficultyLevel, Long trainerId);
    TrainingProgramDto getProgram(Long id);

    List<WorkoutDto> getWorkouts(Long trainerId, Long programId,
                                 String dateFrom, String dateTo, String status);
    WorkoutDto getWorkout(Long id);

    List<ProgressDto> getProgressByClient(Long clientId);
    List<ProgressDto> getProgressByClientAndPeriod(Long clientId, String dateFrom, String dateTo);
    // ЛР4 Mutation-методы
    TrainingProgramDto createTrainingProgram(CreateTrainingProgramInputDto input);
    TrainingProgramDto updateTrainingProgram(Long id, UpdateTrainingProgramInputDto input);
    Boolean archiveTrainingProgram(Long id);
    WorkoutDto createWorkout(CreateWorkoutInputDto input);
    WorkoutDto updateWorkout(Long id, UpdateWorkoutInputDto input);
    WorkoutDto changeWorkoutStatus(Long id, String status);
    ProgressDto logProgress(LogProgressInputDto input);

    // ЛР4 Query с поддержкой фильтрации и сортировки данныхQuery с поддержкой фильтрации и сортировки данных
    List<TrainingProgramDto> getPrograms(
            String type, String difficultyLevel, Long trainerId,
            String sortBy, String sortOrder);

    List<WorkoutDto> getWorkouts(
            Long trainerId, Long programId,
            String dateFrom, String dateTo, String status,
            String sortBy, String sortOrder);
}