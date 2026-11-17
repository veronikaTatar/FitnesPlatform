package spiring.exapmle.apigateway.graphql.mutation;

import com.fitness.user.grpc.GetClientByEmailRequest;
import com.fitness.user.grpc.UserServiceGrpc;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.ContextValue;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;
import spiring.exapmle.apigateway.client.BookingServiceClient;
import spiring.exapmle.apigateway.config.AuthenticatedUserContext;
import spiring.exapmle.apigateway.config.GraphQlAuthenticationInterceptor;
import spiring.exapmle.apigateway.dto.*;

@Controller
@RequiredArgsConstructor
public class BookingMutationResolver {

    private final BookingServiceClient bookingServiceClient;
    private final UserServiceGrpc.UserServiceBlockingStub userService;

    @MutationMapping
    public BookingDto bookWorkout(@Argument Long workoutId,
                                  @ContextValue(GraphQlAuthenticationInterceptor.AUTHENTICATED_USER_CONTEXT_KEY)
                                  SessionUserDto authenticatedUser) {
        return bookingServiceClient.bookWorkout(clientId(authenticatedUser), workoutId);
    }

    @MutationMapping
    public BookingDto cancelBooking(@Argument Long bookingId,
                                    @ContextValue(GraphQlAuthenticationInterceptor.AUTHENTICATED_USER_CONTEXT_KEY)
                                    SessionUserDto authenticatedUser) {
        return bookingServiceClient.cancelBooking(clientId(authenticatedUser), bookingId);
    }

    @MutationMapping
    public SubscriptionDto createSubscription(
            @Argument CreateSubscriptionInputDto input,
            @ContextValue(GraphQlAuthenticationInterceptor.AUTHENTICATED_USER_CONTEXT_KEY)
            SessionUserDto authenticatedUser) {
        return bookingServiceClient.createSubscription(clientId(authenticatedUser), input);
    }

    @MutationMapping
    public SubscriptionDto changeSubscriptionStatus(
            @Argument Long idSubscription,
            @Argument String status) {
        return bookingServiceClient.changeSubscriptionStatus(idSubscription, status);
    }

    @MutationMapping
    public PaymentDto registerPayment(
            @Argument RegisterPaymentInputDto input,
            @ContextValue(GraphQlAuthenticationInterceptor.AUTHENTICATED_USER_CONTEXT_KEY)
            SessionUserDto authenticatedUser) {
        return bookingServiceClient.registerPayment(clientId(authenticatedUser), input);
    }

    private Long clientId(SessionUserDto authenticatedUser) {
        SessionUserDto user = AuthenticatedUserContext.require(authenticatedUser);
        return userService.getClientByEmail(GetClientByEmailRequest.newBuilder()
                .setEmail(user.email()).build()).getIdClient();
    }
}
