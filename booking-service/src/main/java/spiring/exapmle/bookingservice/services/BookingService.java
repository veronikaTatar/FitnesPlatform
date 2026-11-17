package spiring.exapmle.bookingservice.services;

import spiring.exapmle.bookingservice.models.Booking;
import spiring.exapmle.bookingservice.models.Payment;
import spiring.exapmle.bookingservice.models.Subscription;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface BookingService {
    List<Booking> getBookings(Long idClient, Long idWorkout);
    Booking getBookingById(Long id);
    List<Booking> getBookingsByClient(Long idClient);
    List<Booking> getBookingsByClientAndStatus(Long idClient, String status);
    List<Booking> getBookingsByWorkout(Long idWorkout);
    long countActiveBookings(Long idWorkout);
    Booking bookWorkout(Long idClient, Long idWorkout);
    Booking cancelBooking(Long idClient, Long idBooking);

    Subscription getSubscriptionById(Long id);
    List<Subscription> getSubscriptionsByClient(Long idClient);
    Subscription getActiveSubscription(Long idClient);

    Payment getPaymentById(Long id);
    List<Payment> getPaymentsByClient(Long idClient);
    List<Payment> getPaymentsByClientAndPeriod(Long idClient, LocalDateTime from, LocalDateTime to);
    List<Payment> getPaymentsBySubscription(Long idSubscription);
    Subscription createSubscription(Long clientId, Integer typeId, LocalDate startDate);
    Subscription changeSubscriptionStatus(Long id, String status);
    Payment registerPayment(Long clientId, BigDecimal amount, Long subscriptionId,
                            LocalDateTime paymentDate);
}
