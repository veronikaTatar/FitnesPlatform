package spiring.exapmle.apigateway.config;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import spiring.exapmle.apigateway.dto.SessionUserDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthenticatedUserContextTest {

    @Test
    void returnsVerifiedUserFromGraphQlContext() {
        SessionUserDto user = new SessionUserDto(1L, "client@fit.by", "client");

        assertThat(AuthenticatedUserContext.require(user)).isSameAs(user);
    }

    @Test
    void rejectsMissingGraphQlAuthenticationContext() {
        assertThatThrownBy(() -> AuthenticatedUserContext.require(null))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Authentication context is missing");
    }
}
