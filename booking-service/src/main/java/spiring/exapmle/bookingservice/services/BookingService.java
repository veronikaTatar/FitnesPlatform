package spiring.exapmle.bookingservice.services;

import spiring.exapmle.bookingservice.models.Booking;
import spiring.exapmle.bookingservice.models.Payment;
import spiring.exapmle.bookingservice.models.Subscription;
import java.time.LocalDateTime;
import java.util.List;

public interface BookingService {
    Booking getBookingById(Long id);
    List<Booking> getBookingsByClient(Long idClient);
    List<Booking> getBookingsByClientAndStatus(Long idClient, String status);
    List<Booking> getBookingsByWorkout(Long idWorkout);
    long countActiveBookings(Long idWorkout);

    Subscription getSubscriptionById(Long id);
    List<Subscription> getSubscriptionsByClient(Long idClient);
    Subscription getActiveSubscription(Long idClient);

    Payment getPaymentById(Long id);
    List<Payment> getPaymentsByClient(Long idClient);
    List<Payment> getPaymentsByClientAndPeriod(Long idClient, LocalDateTime from, LocalDateTime to);
    List<Payment> getPaymentsBySubscription(Long idSubscription);
}