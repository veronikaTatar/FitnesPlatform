package spiring.exapmle.apigateway.dto;

import lombok.Data;

@Data
public class UpdateProfileInputDto {
    private String name;
    private String phone;
    private String birthDate;
    private String fitnessLevel;
    private String specialization;
}