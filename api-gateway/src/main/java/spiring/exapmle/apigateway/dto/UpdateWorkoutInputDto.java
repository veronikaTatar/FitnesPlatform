package spiring.exapmle.apigateway.dto;

import lombok.Data;

@Data
public class UpdateWorkoutInputDto {
    private String startDatetime;
    private Integer duration;
    private Integer maxCapacity;
}