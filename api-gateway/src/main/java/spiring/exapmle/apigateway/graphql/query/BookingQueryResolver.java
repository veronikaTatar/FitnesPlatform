package spiring.exapmle.apigateway.graphql.query;

import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import spiring.exapmle.apigateway.client.BookingServiceClient;
import spiring.exapmle.apigateway.dto.BookingDto;
import spiring.exapmle.apigateway.dto.PaymentDto;
import spiring.exapmle.apigateway.dto.SubscriptionDto;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class BookingQueryResolver {

    private final BookingServiceClient bookingServiceClient;

    @QueryMapping
    public List<BookingDto> myBookings(
            @Argument String status,
            @Argument String sortBy,
            @Argument String sortOrder) {
        Long clientId = 1L; // в ЛР5 — из JWT
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
    public List<SubscriptionDto> mySubscriptions() {
        // временно: clientId = 1 (в ЛР5 — из сессии)
        return bookingServiceClient.getSubscriptionsByClient(1L);
    }

    @QueryMapping
    public SubscriptionDto subscription(@Argument Long id) {
        return bookingServiceClient.getSubscription(id);
    }

    @QueryMapping
    public List<PaymentDto> myPayments(@Argument String dateFrom,
                                       @Argument String dateTo) {
        // временно: clientId = 1 (в ЛР5 — из сессии)
        return bookingServiceClient.getPaymentsByClient(1L, dateFrom, dateTo);
    }
}