package spiring.exapmle.apigateway.config;

public class GraphQlAccessDeniedException extends RuntimeException {
    public GraphQlAccessDeniedException(String message) {
        super(message);
    }
}
