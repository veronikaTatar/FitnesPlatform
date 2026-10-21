package spiring.exapmle.bookingservice.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import spiring.exapmle.bookingservice.models.Booking;
import spiring.exapmle.bookingservice.models.Payment;
import spiring.exapmle.bookingservice.models.Subscription;
import spiring.exapmle.bookingservice.models.enums.BookingStatus;
import spiring.exapmle.bookingservice.models.enums.SubscriptionStatus;
import spiring.exapmle.bookingservice.repositories.BookingRepository;
import spiring.exapmle.bookingservice.repositories.PaymentRepository;
import spiring.exapmle.bookingservice.repositories.SubscriptionRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentRepository paymentRepository;

    @Override
    public Booking getBookingById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found: " + id));
    }

    @Override
    public List<Booking> getBookingsByClient(Long idClient) {
        return bookingRepository.findByIdClient(idClient);
    }

    @Override
    public List<Booking> getBookingsByClientAndStatus(Long idClient, String status) {
        return bookingRepository.findByIdClientAndStatus(idClient, BookingStatus.valueOf(status));
    }

    @Override
    public List<Booking> getBookingsByWorkout(Long idWorkout) {
        return bookingRepository.findByIdWorkout(idWorkout);
    }

    @Override
    public long countActiveBookings(Long idWorkout) {
        return bookingRepository.countByIdWorkoutAndStatus(idWorkout, BookingStatus.ACTIVE);
    }

    @Override
    public Subscription getSubscriptionById(Long id) {
        return subscriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Subscription not found: " + id));
    }

    @Override
    public List<Subscription> getSubscriptionsByClient(Long idClient) {
        return subscriptionRepository.findByIdClient(idClient);
    }

    @Override
    public Subscription getActiveSubscription(Long idClient) {
        return subscriptionRepository
                .findFirstByIdClientAndStatusOrderByEndDateDesc(idClient, SubscriptionStatus.ACTIVE)
                .orElseThrow(() -> new RuntimeException("Active subscription not found for client: " + idClient));
    }

    @Override
    public Payment getPaymentById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + id));
    }

    @Override
    public List<Payment> getPaymentsByClient(Long idClient) {
        return paymentRepository.findByIdClient(idClient);
    }

    @Override
    public List<Payment> getPaymentsByClientAndPeriod(Long idClient, LocalDateTime from, LocalDateTime to) {
        return paymentRepository.findByIdClientAndPaymentDateBetween(idClient, from, to);
    }

    @Override
    public List<Payment> getPaymentsBySubscription(Long idSubscription) {
        return paymentRepository.findBySubscription_IdSubscription(idSubscription);
    }
}