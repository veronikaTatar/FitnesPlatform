package spiring.exapmle.authservice.service;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import spiring.exapmle.authservice.dto.SessionValidationResponse;
import spiring.exapmle.authservice.exception.AuthException;
import spiring.exapmle.authservice.model.AuthUser;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class SessionService {
    private static final String SESSION_KEY_PREFIX = "auth:session:";

    private final StringRedisTemplate redisTemplate;
    private final JwtService jwtService;

    public void createSession(String token, AuthUser user) {
        Claims claims = jwtService.parseClaims(token);
        Duration ttl = remainingLifetime(claims.getExpiration());
        redisTemplate.opsForValue().set(
                keyFor(claims),
                user.getIdUser() + "|" + user.getEmail() + "|" + user.getRole().getName(),
                ttl
        );
    }

    public SessionValidationResponse validateSession(String authorizationHeader) {
        String token = extractBearerToken(authorizationHeader);
        Claims claims = jwtService.parseClaims(token);
        String storedSession = redisTemplate.opsForValue().get(keyFor(claims));
        if (storedSession == null) {
            throw reauthenticationRequired();
        }

        Long userId = claims.get("userId", Long.class);
        String email = claims.getSubject();
        String role = claims.get("role", String.class);
        String expectedSession = userId + "|" + email + "|" + role;
        if (!expectedSession.equals(storedSession)) {
            throw reauthenticationRequired();
        }
        return new SessionValidationResponse(userId, email, role);
    }

    private String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw reauthenticationRequired();
        }
        String token = authorizationHeader.substring("Bearer ".length()).trim();
        if (token.isEmpty()) {
            throw reauthenticationRequired();
        }
        return token;
    }

    private String keyFor(Claims claims) {
        String tokenId = claims.getId();
        if (tokenId == null || tokenId.isBlank()) {
            throw reauthenticationRequired();
        }
        return SESSION_KEY_PREFIX + tokenId;
    }

    private Duration remainingLifetime(Date expiration) {
        Duration ttl = Duration.between(Instant.now(), expiration.toInstant());
        if (ttl.isZero() || ttl.isNegative()) {
            throw reauthenticationRequired();
        }
        return ttl;
    }

    private AuthException reauthenticationRequired() {
        return new AuthException(HttpStatus.UNAUTHORIZED,
                "Active session not found. Please authenticate again");
    }
}
