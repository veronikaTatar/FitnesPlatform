package spiring.exapmle.bookingservice.services;

import com.fitness.workout.grpc.CheckAvailabilityRequest;
import com.fitness.workout.grpc.ReleaseSlotRequest;
import com.fitness.workout.grpc.ReserveSlotRequest;
import com.fitness.workout.grpc.WorkoutServiceGrpc;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
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

    @GrpcClient("workoutService")
    private WorkoutServiceGrpc.WorkoutServiceBlockingStub workoutService;

    @Override
    public List<Booking> getBookings(Long idClient, Long idWorkout) {
        return bookingRepository.findAll().stream()
                .filter(b -> idClient == null || idClient.equals(b.getIdClient()))
                .filter(b -> idWorkout == null || idWorkout.equals(b.getIdWorkout()))
                .toList();
    }

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
    @Transactional
    public Booking bookWorkout(Long idClient, Long idWorkout) {
        Subscription subscription = getActiveSubscription(idClient);
        if (subscription.getRemainingSessions() <= 0) {
            throw Status.FAILED_PRECONDITION
                    .withDescription("No remaining sessions in active subscription")
                    .asRuntimeException();
        }

        try {
            var availability = workoutService.checkAvailability(
                    CheckAvailabilityRequest.newBuilder().setIdWorkout(idWorkout).build());
            if (!availability.getAvailable()) {
                throw Status.FAILED_PRECONDITION
                        .withDescription("Workout is full or is not planned")
                        .asRuntimeException();
            }
        } catch (StatusRuntimeException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw Status.UNAVAILABLE.withDescription("Workout Service is unavailable")
                    .withCause(exception).asRuntimeException();
        }

        boolean reserved = false;
        try {
            var reservation = workoutService.reserveSlot(
                    ReserveSlotRequest.newBuilder().setIdWorkout(idWorkout).build());
            reserved = reservation.getSuccess();
            if (!reserved) {
                throw Status.FAILED_PRECONDITION
                        .withDescription("No free slots for workout: " + idWorkout)
                        .asRuntimeException();
            }

            subscription.setRemainingSessions(subscription.getRemainingSessions() - 1);
            subscriptionRepository.save(subscription);

            Booking booking = new Booking();
            booking.setIdClient(idClient);
            booking.setIdWorkout(idWorkout);
            booking.setStatus(BookingStatus.ACTIVE);
            booking.setBookingDate(LocalDateTime.now());
            return bookingRepository.save(booking);
        } catch (RuntimeException exception) {
            if (reserved) {
                try {
                    workoutService.releaseSlot(ReleaseSlotRequest.newBuilder()
                            .setIdWorkout(idWorkout).build());
                } catch (RuntimeException compensationFailure) {
                    exception.addSuppressed(compensationFailure);
                }
            }
            throw exception;
        }
    }

    @Override
    @Transactional
    public Booking cancelBooking(Long idClient, Long idBooking) {
        Booking booking = getBookingById(idBooking);
        if (!idClient.equals(booking.getIdClient())) {
            throw Status.PERMISSION_DENIED
                    .withDescription("Booking does not belong to authenticated client")
                    .asRuntimeException();
        }
        if (booking.getStatus() != BookingStatus.ACTIVE) {
            throw Status.FAILED_PRECONDITION
                    .withDescription("Only active bookings can be cancelled")
                    .asRuntimeException();
        }

        Subscription activeSubscription = subscriptionRepository
                .findFirstByIdClientAndStatusOrderByEndDateDesc(
                        idClient, SubscriptionStatus.ACTIVE).orElse(null);
        boolean released;
        try {
            released = workoutService.releaseSlot(ReleaseSlotRequest.newBuilder()
                    .setIdWorkout(booking.getIdWorkout()).build()).getSuccess();
        } catch (RuntimeException exception) {
            throw Status.UNAVAILABLE.withDescription("Workout Service is unavailable")
                    .withCause(exception).asRuntimeException();
        }
        if (!released) {
            throw Status.FAILED_PRECONDITION
                    .withDescription("Workout reservation could not be released")
                    .asRuntimeException();
        }

        try {
            booking.setStatus(BookingStatus.CANCELLED);
            if (activeSubscription != null) {
                activeSubscription.setRemainingSessions(
                        activeSubscription.getRemainingSessions() + 1);
                subscriptionRepository.save(activeSubscription);
            }
            return bookingRepository.save(booking);
        } catch (RuntimeException exception) {
            try {
                workoutService.reserveSlot(ReserveSlotRequest.newBuilder()
                        .setIdWorkout(booking.getIdWorkout()).build());
            } catch (RuntimeException compensationFailure) {
                exception.addSuppressed(compensationFailure);
            }
            throw exception;
        }
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
