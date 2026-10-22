package spiring.exapmle.apigateway.config;

import lombok.RequiredArgsConstructor;
import org.springframework.graphql.server.WebGraphQlInterceptor;
import org.springframework.graphql.server.WebGraphQlRequest;
import org.springframework.graphql.server.WebGraphQlResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import spiring.exapmle.apigateway.client.AuthServiceClient;
import spiring.exapmle.apigateway.dto.SessionUserDto;

@Component
@RequiredArgsConstructor
public class GraphQlAuthenticationInterceptor implements WebGraphQlInterceptor {
    public static final String AUTHENTICATED_USER_CONTEXT_KEY = "authenticatedUser";

    private final AuthServiceClient authServiceClient;
    private final GraphQlAuthorizationPolicy authorizationPolicy;

    @Override
    public Mono<WebGraphQlResponse> intercept(WebGraphQlRequest request, Chain chain) {
        if (authorizationPolicy.isPublicOperation(request.getDocument())) {
            return chain.next(request);
        }

        String authorizationHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        SessionUserDto authenticatedUser;
        try {
            authenticatedUser = authServiceClient.validateSession(authorizationHeader);
        } catch (IllegalArgumentException exception) {
            return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, exception.getMessage()));
        }
        try {
            authorizationPolicy.authorize(request.getDocument(), authenticatedUser);
        } catch (GraphQlAccessDeniedException exception) {
            return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, exception.getMessage()));
        }
        request.configureExecutionInput((executionInput, builder) -> builder
                .graphQLContext(context -> context.put(AUTHENTICATED_USER_CONTEXT_KEY, authenticatedUser))
                .build());
        return chain.next(request);
    }

}
