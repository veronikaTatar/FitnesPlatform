package spiring.exapmle.apigateway.client;

import spiring.exapmle.apigateway.dto.*;

import java.util.List;

public interface BookingServiceClient {
    List<BookingDto> getBookings(Long clientId, Long workoutId);
    BookingDto getBooking(Long id);
    List<BookingDto> getBookingsByClient(Long clientId, String status);

    List<SubscriptionDto> getSubscriptionsByClient(Long clientId);
    SubscriptionDto getSubscription(Long id);

    List<PaymentDto> getPaymentsByClient(Long clientId, String dateFrom, String dateTo);
    // ЛР4 Mutation-методы
    BookingDto bookWorkout(Long workoutId);
    BookingDto cancelBooking(Long bookingId);
    SubscriptionDto createSubscription(CreateSubscriptionInputDto input);
    SubscriptionDto changeSubscriptionStatus(Long id, String status);
    PaymentDto registerPayment(RegisterPaymentInputDto input);

    // ЛР4 Query с поддержкой фильтрации и сортировки данных
    List<BookingDto> getBookingsByClient(
            Long clientId, String status,
            String sortBy, String sortOrder);
}