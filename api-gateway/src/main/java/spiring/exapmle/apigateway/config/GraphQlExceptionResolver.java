package spiring.exapmle.apigateway.config;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class GraphQlExceptionResolver extends DataFetcherExceptionResolverAdapter {

    @Override
    protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {
        StatusRuntimeException grpcException = findGrpcException(ex);
        if (grpcException != null) {
            Status.Code code = grpcException.getStatus().getCode();
            ErrorType errorType = switch (code) {
                case NOT_FOUND -> ErrorType.NOT_FOUND;
                case INVALID_ARGUMENT, FAILED_PRECONDITION, PERMISSION_DENIED -> ErrorType.BAD_REQUEST;
                default -> ErrorType.INTERNAL_ERROR;
            };
            String message = grpcException.getStatus().getDescription();
            if (message == null || message.isBlank()) {
                message = code == Status.Code.UNAVAILABLE || code == Status.Code.DEADLINE_EXCEEDED
                        ? "A dependent service is temporarily unavailable"
                        : "Inter-service request failed";
            }
            return GraphqlErrorBuilder.newError()
                    .message(message)
                    .path(env.getExecutionStepInfo().getPath())
                    .location(env.getField().getSourceLocation())
                    .errorType(errorType)
                    .extensions(Map.of("grpcStatus", code.name()))
                    .build();
        }
        // Раскрываем сообщения RuntimeException клиенту
        if (ex instanceof RuntimeException) {
            return GraphqlErrorBuilder.newError()
                    .message(ex.getMessage())
                    .path(env.getExecutionStepInfo().getPath())
                    .location(env.getField().getSourceLocation())
                    .errorType(ErrorType.BAD_REQUEST)
                    .build();
        }
        return null;   // другие исключения обрабатываются стандартно
    }

    private static StatusRuntimeException findGrpcException(Throwable exception) {
        Throwable current = exception;
        while (current != null) {
            if (current instanceof StatusRuntimeException grpcException) {
                return grpcException;
            }
            current = current.getCause();
        }
        return null;
    }
}
