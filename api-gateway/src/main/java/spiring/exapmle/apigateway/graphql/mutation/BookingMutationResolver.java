package spiring.exapmle.apigateway.graphql.mutation;

import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;
import spiring.exapmle.apigateway.client.BookingServiceClient;
import spiring.exapmle.apigateway.dto.*;

@Controller
@RequiredArgsConstructor
public class BookingMutationResolver {

    private final BookingServiceClient bookingServiceClient;

    @MutationMapping
    public BookingDto bookWorkout(@Argument Long workoutId) {
        return bookingServiceClient.bookWorkout(workoutId);
    }

    @MutationMapping
    public BookingDto cancelBooking(@Argument Long bookingId) {
        return bookingServiceClient.cancelBooking(bookingId);
    }

    @MutationMapping
    public SubscriptionDto createSubscription(
            @Argument CreateSubscriptionInputDto input) {
        return bookingServiceClient.createSubscription(input);
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