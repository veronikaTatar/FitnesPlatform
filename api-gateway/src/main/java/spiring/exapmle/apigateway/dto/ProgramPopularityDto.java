package spiring.exapmle.apigateway.dto;

import lombok.Data;

@Data
public class ProgramPopularityDto {
    private Long idTrainingProgram;
    private Integer bookingCount;
    private String period;
}