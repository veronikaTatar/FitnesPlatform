package spiring.exapmle.authservice.dto;

public record AuthResponse(String token, UserResponse user) {
}
