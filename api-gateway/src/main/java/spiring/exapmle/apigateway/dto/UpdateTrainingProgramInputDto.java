package spiring.exapmle.apigateway.dto;

import lombok.Data;

@Data
public class UpdateTrainingProgramInputDto {
    private String name;
    private String description;
    private String type;
    private String difficultyLevel;
    private Integer duration;
}