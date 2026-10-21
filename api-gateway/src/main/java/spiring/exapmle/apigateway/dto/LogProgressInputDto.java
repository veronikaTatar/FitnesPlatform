package spiring.exapmle.apigateway.dto;

import lombok.Data;

@Data
public class LogProgressInputDto {
    private Long idClient;
    private Long idBooking;
    private Float value;
    private String trainerComment;
}