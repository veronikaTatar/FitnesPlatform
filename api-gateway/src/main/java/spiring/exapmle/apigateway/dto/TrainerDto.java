package spiring.exapmle.apigateway.dto;

import lombok.Data;

@Data
public class TrainerDto {
    private Long idTrainer;
    private String specialization;
    private Float rating;
}