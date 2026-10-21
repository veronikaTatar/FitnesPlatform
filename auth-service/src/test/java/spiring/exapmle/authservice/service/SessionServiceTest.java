package spiring.exapmle.authservice.service;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import spiring.exapmle.authservice.dto.SessionValidationResponse;
import spiring.exapmle.authservice.exception.AuthException;
import spiring.exapmle.authservice.model.AuthUser;
import spiring.exapmle.authservice.model.Role;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessionServiceTest {
    @Mock StringRedisTemplate redisTemplate;
    @Mock ValueOperations<String, String> values;

    private JwtService jwtService;
    private SessionService sessionService;
    private AuthUser user;

    @BeforeEach
    void setUp() throws Exception {
        jwtService = new JwtService("test-secret-key-with-at-least-thirty-two-bytes", 3_600_000);
        sessionService = new SessionService(redisTemplate, jwtService);
        when(redisTemplate.opsForValue()).thenReturn(values);

        Role role = new Role();
        setField(role, "idRole", 1L);
        setField(role, "name", "client");
        user = new AuthUser("Клиент", "client@fit.by", "$2a$hash", null, role);
        setField(user, "idUser", 7L);
    }

    @Test
    void createsAndValidatesSessionForToken() {
        String token = jwtService.generateToken(user);
        Claims claims = jwtService.parseClaims(token);
        String key = "auth:session:" + claims.getId();
        String value = "7|client@fit.by|client";

        sessionService.createSession(token, user);
        verify(values).set(eq(key), eq(value), any());
        when(values.get(key)).thenReturn(value);

        SessionValidationResponse response = sessionService.validateSession("Bearer " + token);
        assertThat(response.userId()).isEqualTo(7L);
        assertThat(response.role()).isEqualTo("client");
    }

    @Test
    void rejectsTokenWithoutActiveSession() {
        String token = jwtService.generateToken(user);
        Claims claims = jwtService.parseClaims(token);
        when(values.get("auth:session:" + claims.getId())).thenReturn(null);

        assertThatThrownBy(() -> sessionService.validateSession("Bearer " + token))
                .isInstanceOf(AuthException.class)
                .hasMessageContaining("Please authenticate again");
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
