package spiring.exapmle.apigateway.dto;

import lombok.Data;

@Data
public class ProgressDto {
    private Long idProgress;
    private String date;
    private Float value;
    private String trainerComment;
    private ClientDto client;
}