package spiring.exapmle.apigateway.dto;

import lombok.Data;

@Data
public class WorkoutDto {
    private Long idWorkout;
    private String startDatetime;
    private Integer duration;
    private String status;
    private Integer maxCapacity;
    private TrainingProgramDto trainingProgram;
    private TrainerDto trainer;
}