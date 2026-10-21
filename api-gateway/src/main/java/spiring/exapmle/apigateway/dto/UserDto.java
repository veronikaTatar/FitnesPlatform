package spiring.exapmle.apigateway.dto;

import lombok.Data;

@Data
public class UserDto {
    private Long idUser;
    private String name;
    private String email;
    private String phone;
    private RoleDto role;
    private ClientDto client;
    private TrainerDto trainer;
}