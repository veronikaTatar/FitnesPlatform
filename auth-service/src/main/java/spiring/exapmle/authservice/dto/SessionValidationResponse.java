package spiring.exapmle.authservice.dto;

public record SessionValidationResponse(Long userId, String email, String role) {
}
