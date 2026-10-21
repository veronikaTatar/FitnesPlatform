package spiring.exapmle.apigateway.config;

import graphql.language.Document;
import graphql.language.Field;
import graphql.language.OperationDefinition;
import graphql.parser.Parser;
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

    @Override
    public Mono<WebGraphQlResponse> intercept(WebGraphQlRequest request, Chain chain) {
        if (isPublicOperation(request.getDocument())) {
            return chain.next(request);
        }

        String authorizationHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        SessionUserDto authenticatedUser;
        try {
            authenticatedUser = authServiceClient.validateSession(authorizationHeader);
        } catch (IllegalArgumentException exception) {
            return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, exception.getMessage()));
        }
        request.configureExecutionInput((executionInput, builder) -> builder
                .graphQLContext(context -> context.put(AUTHENTICATED_USER_CONTEXT_KEY, authenticatedUser))
                .build());
        return chain.next(request);
    }

    private boolean isPublicOperation(String document) {
        try {
            Document parsedDocument = new Parser().parseDocument(document);
            if (parsedDocument.getDefinitions().size() != 1
                    || !(parsedDocument.getDefinitions().getFirst() instanceof OperationDefinition operation)
                    || operation.getOperation() != OperationDefinition.Operation.MUTATION
                    || operation.getSelectionSet().getSelections().size() != 1
                    || !(operation.getSelectionSet().getSelections().getFirst() instanceof Field field)) {
                return false;
            }

            return field.getName().equals("register") || field.getName().equals("login");
        } catch (RuntimeException exception) {
            return false;
        }
    }
}
