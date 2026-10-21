package spiring.exapmle.authservice.dto;

import spiring.exapmle.authservice.model.AuthUser;

public record UserResponse(Long idUser, String name, String email, String phone, RoleResponse role) {

    public static UserResponse from(AuthUser user) {
        return new UserResponse(
                user.getIdUser(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                new RoleResponse(user.getRole().getIdRole(), user.getRole().getName())
        );
    }
}
