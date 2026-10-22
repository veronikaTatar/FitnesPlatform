package spiring.exapmle.apigateway.config;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import spiring.exapmle.apigateway.dto.SessionUserDto;

public final class AuthenticatedUserContext {
    private AuthenticatedUserContext() {
    }

    public static SessionUserDto require(SessionUserDto user) {
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Authentication context is missing. Please authenticate again");
        }
        return user;
    }
}
