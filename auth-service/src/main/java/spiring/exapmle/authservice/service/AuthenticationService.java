package spiring.exapmle.authservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spiring.exapmle.authservice.dto.AuthResponse;
import spiring.exapmle.authservice.dto.LoginRequest;
import spiring.exapmle.authservice.dto.RegisterRequest;
import spiring.exapmle.authservice.dto.UserResponse;
import spiring.exapmle.authservice.exception.AuthException;
import spiring.exapmle.authservice.model.AuthUser;
import spiring.exapmle.authservice.model.Role;
import spiring.exapmle.authservice.repository.AuthUserRepository;
import spiring.exapmle.authservice.repository.RoleRepository;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final AuthUserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new AuthException(HttpStatus.CONFLICT, "User with this email already exists");
        }

        Role role = roleRepository.findByNameIgnoreCase(request.role())
                .orElseThrow(() -> new AuthException(HttpStatus.BAD_REQUEST, "Unknown role"));

        AuthUser user = userRepository.save(new AuthUser(
                request.name().trim(),
                email,
                passwordEncoder.encode(request.password()),
                normalizePhone(request.phone()),
                role
        ));
        return responseFor(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        AuthUser user = userRepository.findByEmailIgnoreCase(request.email().trim())
                .orElseThrow(this::invalidCredentials);
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw invalidCredentials();
        }
        return responseFor(user);
    }

    private AuthResponse responseFor(AuthUser user) {
        return new AuthResponse(jwtService.generateToken(user), UserResponse.from(user));
    }

    private AuthException invalidCredentials() {
        return new AuthException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
    }

    private String normalizePhone(String phone) {
        return phone == null || phone.isBlank() ? null : phone.trim();
    }
}
