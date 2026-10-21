package spiring.exapmle.apigateway.dto;

import lombok.Data;

@Data
public class RegisterInputDto {
    private String name;
    private String email;
    private String password;
    private String phone;
    private String role;          // client | trainer
    private String birthDate;
    private String fitnessLevel;
    private String specialization;
}