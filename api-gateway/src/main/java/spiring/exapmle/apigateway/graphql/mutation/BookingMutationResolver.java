package spiring.exapmle.apigateway.graphql.mutation;

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

    @MutationMapping
    public BookingDto bookWorkout(@Argument Long workoutId,
                                  @ContextValue(GraphQlAuthenticationInterceptor.AUTHENTICATED_USER_CONTEXT_KEY)
                                  SessionUserDto authenticatedUser) {
        return bookingServiceClient.bookWorkout(
                AuthenticatedUserContext.require(authenticatedUser).userId(), workoutId);
    }

    @MutationMapping
    public BookingDto cancelBooking(@Argument Long bookingId,
                                    @ContextValue(GraphQlAuthenticationInterceptor.AUTHENTICATED_USER_CONTEXT_KEY)
                                    SessionUserDto authenticatedUser) {
        return bookingServiceClient.cancelBooking(
                AuthenticatedUserContext.require(authenticatedUser).userId(), bookingId);
    }

    @MutationMapping
    public SubscriptionDto createSubscription(
            @Argument CreateSubscriptionInputDto input,
            @ContextValue(GraphQlAuthenticationInterceptor.AUTHENTICATED_USER_CONTEXT_KEY)
            SessionUserDto authenticatedUser) {
        return bookingServiceClient.createSubscription(
                AuthenticatedUserContext.require(authenticatedUser).userId(), input);
    }

    @MutationMapping
    public SubscriptionDto changeSubscriptionStatus(
            @Argument Long idSubscription,
            @Argument String status) {
        return bookingServiceClient.changeSubscriptionStatus(idSubscription, status);
    }

    @MutationMapping
    public PaymentDto registerPayment(
            @Argument RegisterPaymentInputDto input) {
        return bookingServiceClient.registerPayment(input);
    }
}
