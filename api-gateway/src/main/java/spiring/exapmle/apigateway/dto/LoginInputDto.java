package spiring.exapmle.apigateway.dto;

import lombok.Data;

@Data
public class LoginInputDto {
    private String email;
    private String password;
}