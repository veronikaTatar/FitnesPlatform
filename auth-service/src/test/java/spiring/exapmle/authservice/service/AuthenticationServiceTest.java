package spiring.exapmle.authservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import spiring.exapmle.authservice.dto.AuthResponse;
import spiring.exapmle.authservice.dto.LoginRequest;
import spiring.exapmle.authservice.dto.RegisterRequest;
import spiring.exapmle.authservice.exception.AuthException;
import spiring.exapmle.authservice.model.AuthUser;
import spiring.exapmle.authservice.model.Role;
import spiring.exapmle.authservice.repository.AuthUserRepository;
import spiring.exapmle.authservice.repository.RoleRepository;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {
    @Mock AuthUserRepository userRepository;
    @Mock RoleRepository roleRepository;

    private AuthenticationService service;
    private Role clientRole;

    @BeforeEach
    void setUp() throws Exception {
        clientRole = new Role();
        setId(clientRole, "idRole", 1L);
        setId(clientRole, "name", "client");
        JwtService jwtService = new JwtService(
                "test-secret-key-with-at-least-thirty-two-bytes", 3_600_000);
        service = new AuthenticationService(
                userRepository, roleRepository, new BCryptPasswordEncoder(), jwtService);
    }

    @Test
    void registerHashesPasswordAndReturnsJwt() throws Exception {
        when(userRepository.existsByEmailIgnoreCase("new@fit.by")).thenReturn(false);
        when(roleRepository.findByNameIgnoreCase("client")).thenReturn(Optional.of(clientRole));
        when(userRepository.save(any(AuthUser.class))).thenAnswer(invocation -> {
            AuthUser user = invocation.getArgument(0);
            setId(user, "idUser", 10L);
            return user;
        });

        AuthResponse response = service.register(new RegisterRequest(
                "Новый Клиент", "NEW@FIT.BY", "password123", "+375291234567", "client"));

        assertThat(response.token()).isNotBlank();
        assertThat(response.user().email()).isEqualTo("new@fit.by");
        assertThat(response.user().role().name()).isEqualTo("client");
    }

    @Test
    void loginRejectsWrongPassword() {
        AuthUser user = new AuthUser("Клиент", "client@fit.by",
                new BCryptPasswordEncoder().encode("correct-password"), null, clientRole);
        when(userRepository.findByEmailIgnoreCase("client@fit.by")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.login(new LoginRequest("client@fit.by", "wrong-password")))
                .isInstanceOf(AuthException.class)
                .satisfies(error -> assertThat(((AuthException) error).getStatus())
                        .isEqualTo(HttpStatus.UNAUTHORIZED));
    }

    private static void setId(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
