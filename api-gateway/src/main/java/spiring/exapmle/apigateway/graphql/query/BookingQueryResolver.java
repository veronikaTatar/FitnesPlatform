package spiring.exapmle.apigateway.graphql.query;

import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.ContextValue;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import spiring.exapmle.apigateway.client.BookingServiceClient;
import spiring.exapmle.apigateway.config.AuthenticatedUserContext;
import spiring.exapmle.apigateway.config.GraphQlAuthenticationInterceptor;
import spiring.exapmle.apigateway.dto.BookingDto;
import spiring.exapmle.apigateway.dto.PaymentDto;
import spiring.exapmle.apigateway.dto.SubscriptionDto;
import spiring.exapmle.apigateway.dto.SessionUserDto;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class BookingQueryResolver {

    private final BookingServiceClient bookingServiceClient;

    @QueryMapping
    public List<BookingDto> myBookings(
            @Argument String status,
            @Argument String sortBy,
            @Argument String sortOrder,
            @ContextValue(GraphQlAuthenticationInterceptor.AUTHENTICATED_USER_CONTEXT_KEY)
            SessionUserDto authenticatedUser) {
        Long clientId = AuthenticatedUserContext.require(authenticatedUser).userId();
        return bookingServiceClient.getBookingsByClient(
                clientId, status, sortBy, sortOrder);
    }

    @QueryMapping
    public List<BookingDto> bookings(@Argument Long clientId,
                                     @Argument Long workoutId) {
        return bookingServiceClient.getBookings(clientId, workoutId);
    }

    @QueryMapping
    public BookingDto booking(@Argument Long id) {
        return bookingServiceClient.getBooking(id);
    }

    @QueryMapping
    public List<SubscriptionDto> mySubscriptions(
            @ContextValue(GraphQlAuthenticationInterceptor.AUTHENTICATED_USER_CONTEXT_KEY)
            SessionUserDto authenticatedUser) {
        return bookingServiceClient.getSubscriptionsByClient(
                AuthenticatedUserContext.require(authenticatedUser).userId());
    }

    @QueryMapping
    public SubscriptionDto subscription(@Argument Long id) {
        return bookingServiceClient.getSubscription(id);
    }

    @QueryMapping
    public List<PaymentDto> myPayments(@Argument String dateFrom,
                                       @Argument String dateTo,
                                       @ContextValue(GraphQlAuthenticationInterceptor.AUTHENTICATED_USER_CONTEXT_KEY)
                                       SessionUserDto authenticatedUser) {
        return bookingServiceClient.getPaymentsByClient(
                AuthenticatedUserContext.require(authenticatedUser).userId(), dateFrom, dateTo);
    }
}
