package spiring.exapmle.apigateway.dto;

import lombok.Data;

@Data
public class AuthPayloadDto {
    private String token;
    private UserDto user;
}