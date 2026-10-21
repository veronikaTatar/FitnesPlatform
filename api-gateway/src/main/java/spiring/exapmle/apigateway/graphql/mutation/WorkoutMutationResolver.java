package spiring.exapmle.apigateway.graphql.mutation;

import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;
import spiring.exapmle.apigateway.client.WorkoutServiceClient;
import spiring.exapmle.apigateway.dto.*;

@Controller
@RequiredArgsConstructor
public class WorkoutMutationResolver {

    private final WorkoutServiceClient workoutServiceClient;

    @MutationMapping
    public TrainingProgramDto createTrainingProgram(
            @Argument CreateTrainingProgramInputDto input) {
        return workoutServiceClient.createTrainingProgram(input);
    }

    @MutationMapping
    public TrainingProgramDto updateTrainingProgram(
            @Argument Long idTrainingProgram,
            @Argument UpdateTrainingProgramInputDto input) {
        return workoutServiceClient.updateTrainingProgram(idTrainingProgram, input);
    }

    @MutationMapping
    public Boolean archiveTrainingProgram(@Argument Long idTrainingProgram) {
        return workoutServiceClient.archiveTrainingProgram(idTrainingProgram);
    }

    @MutationMapping
    public WorkoutDto createWorkout(@Argument CreateWorkoutInputDto input) {
        return workoutServiceClient.createWorkout(input);
    }

    @MutationMapping
    public WorkoutDto updateWorkout(
            @Argument Long idWorkout,
            @Argument UpdateWorkoutInputDto input) {
        return workoutServiceClient.updateWorkout(idWorkout, input);
    }

    @MutationMapping
    public WorkoutDto changeWorkoutStatus(
            @Argument Long idWorkout,
            @Argument String status) {
        return workoutServiceClient.changeWorkoutStatus(idWorkout, status);
    }

    @MutationMapping
    public ProgressDto logProgress(@Argument LogProgressInputDto input) {
        return workoutServiceClient.logProgress(input);
    }
}