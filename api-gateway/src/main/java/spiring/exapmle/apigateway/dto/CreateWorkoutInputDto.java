package spiring.exapmle.apigateway.dto;

import lombok.Data;

@Data
public class CreateWorkoutInputDto {
    private Long idTrainingProgram;
    private String startDatetime;
    private Integer duration;
    private Integer maxCapacity;
}