package spiring.exapmle.apigateway.dto;

import lombok.Data;

@Data
public class ClientDto {
    private Long idClient;
    private String birthDate;
    private String fitnessLevel;
}