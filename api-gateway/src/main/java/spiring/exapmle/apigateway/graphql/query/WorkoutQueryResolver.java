package spiring.exapmle.apigateway.graphql.query;

import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import spiring.exapmle.apigateway.client.WorkoutServiceClient;
import spiring.exapmle.apigateway.dto.ProgressDto;
import spiring.exapmle.apigateway.dto.TrainingProgramDto;
import spiring.exapmle.apigateway.dto.WorkoutDto;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class WorkoutQueryResolver {

    private final WorkoutServiceClient workoutServiceClient;

    @QueryMapping
    public List<TrainingProgramDto> trainingPrograms(
            @Argument String type,
            @Argument String difficultyLevel,
            @Argument Long trainerId,
            @Argument String sortBy,
            @Argument String sortOrder) {
        return workoutServiceClient.getPrograms(
                type, difficultyLevel, trainerId, sortBy, sortOrder);
    }

    @QueryMapping
    public TrainingProgramDto trainingProgram(@Argument Long id) {
        return workoutServiceClient.getProgram(id);
    }

    @QueryMapping
    public List<WorkoutDto> workouts(
            @Argument Long trainerId,
            @Argument Long programId,
            @Argument String dateFrom,
            @Argument String dateTo,
            @Argument String status,
            @Argument String sortBy,
            @Argument String sortOrder) {
        return workoutServiceClient.getWorkouts(
                trainerId, programId, dateFrom, dateTo,
                status, sortBy, sortOrder);
    }


    @QueryMapping
    public WorkoutDto workout(@Argument Long id) {
        return workoutServiceClient.getWorkout(id);
    }

    @QueryMapping
    public List<ProgressDto> myProgress(
            @Argument String dateFrom,
            @Argument String dateTo) {
        // временно: clientId = 1 (в ЛР5 будет браться из сессии)
        return workoutServiceClient.getProgressByClientAndPeriod(1L, dateFrom, dateTo);
    }

    @QueryMapping
    public List<ProgressDto> clientProgress(@Argument Long clientId) {
        return workoutServiceClient.getProgressByClient(clientId);
    }
}