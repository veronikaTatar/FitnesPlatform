package spiring.exapmle.bookingservice.services;

import com.fitness.workout.grpc.CheckAvailabilityRequest;
import com.fitness.workout.grpc.ReleaseSlotRequest;
import com.fitness.workout.grpc.ReserveSlotRequest;
import com.fitness.workout.grpc.WorkoutServiceGrpc;
import by.bsuir.fitness.events.BookingCreatedEvent;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
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
import java.time.LocalDate;
import java.math.BigDecimal;
import spiring.exapmle.bookingservice.models.enums.PaymentStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private static final Logger log = LoggerFactory.getLogger(BookingServiceImpl.class);
    private static final long WORKOUT_RPC_TIMEOUT_MS = 2500;
    private static final int AVAILABILITY_MAX_ATTEMPTS = 3;

    private final BookingRepository bookingRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentRepository paymentRepository;
    private final ApplicationEventPublisher eventPublisher;

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
            var availability = checkAvailabilityWithRetry(idWorkout);
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

        var reservation = callWorkout(() -> workoutService
                .withDeadlineAfter(WORKOUT_RPC_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                .reserveSlot(ReserveSlotRequest.newBuilder().setIdWorkout(idWorkout).build()));
        if (!reservation.getSuccess()) {
            throw Status.FAILED_PRECONDITION
                    .withDescription("No free slots for workout: " + idWorkout)
                    .asRuntimeException();
        }
        registerReservationCompensation(idWorkout);

        subscription.setRemainingSessions(subscription.getRemainingSessions() - 1);
        subscriptionRepository.save(subscription);

        Booking booking = new Booking();
        booking.setIdClient(idClient);
        booking.setIdWorkout(idWorkout);
        booking.setStatus(BookingStatus.ACTIVE);
        booking.setBookingDate(LocalDateTime.now());
        Booking saved = bookingRepository.save(booking);
        eventPublisher.publishEvent(new BookingCreatedEvent(
                UUID.randomUUID().toString(), saved.getIdBooking(), idClient,
                idWorkout, Instant.now()));
        return saved;
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
            released = callWorkout(() -> workoutService
                    .withDeadlineAfter(WORKOUT_RPC_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                    .releaseSlot(ReleaseSlotRequest.newBuilder()
                            .setIdWorkout(booking.getIdWorkout()).build())).getSuccess();
        } catch (StatusRuntimeException exception) {
            throw exception;
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
                callWorkout(() -> workoutService
                        .withDeadlineAfter(WORKOUT_RPC_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                        .reserveSlot(ReserveSlotRequest.newBuilder()
                                .setIdWorkout(booking.getIdWorkout()).build()));
            } catch (RuntimeException compensationFailure) {
                exception.addSuppressed(compensationFailure);
            }
            throw exception;
        }
    }

    private com.fitness.workout.grpc.CheckAvailabilityResponse checkAvailabilityWithRetry(Long idWorkout) {
        StatusRuntimeException lastFailure = null;
        for (int attempt = 1; attempt <= AVAILABILITY_MAX_ATTEMPTS; attempt++) {
            try {
                return callWorkout(() -> workoutService
                        .withDeadlineAfter(WORKOUT_RPC_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                        .checkAvailability(CheckAvailabilityRequest.newBuilder()
                                .setIdWorkout(idWorkout).build()));
            } catch (StatusRuntimeException exception) {
                Status.Code code = exception.getStatus().getCode();
                boolean transientFailure = code == Status.Code.UNAVAILABLE
                        || code == Status.Code.DEADLINE_EXCEEDED;
                if (!transientFailure || attempt == AVAILABILITY_MAX_ATTEMPTS) {
                    throw exception;
                }
                lastFailure = exception;
                pauseBeforeRetry(attempt, exception);
            }
        }
        throw lastFailure;
    }

    private <T> T callWorkout(Supplier<T> call) {
        try {
            return call.get();
        } catch (StatusRuntimeException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw Status.UNAVAILABLE.withDescription("Workout Service is unavailable")
                    .withCause(exception).asRuntimeException();
        }
    }

    private void pauseBeforeRetry(int attempt, StatusRuntimeException previousFailure) {
        long backoffMs = 100L * attempt;
        try {
            Thread.sleep(backoffMs);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw Status.CANCELLED.withDescription("Interrupted while retrying Workout Service")
                    .withCause(interrupted).asRuntimeException();
        }
        log.warn("Workout Service availability check failed; retrying attempt {}/{} after {} ms: {}",
                attempt + 1, AVAILABILITY_MAX_ATTEMPTS, backoffMs,
                previousFailure.getStatus().getCode());
    }

    private void registerReservationCompensation(Long idWorkout) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            throw new IllegalStateException("Booking transaction synchronization is not active");
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED) {
                    try {
                        callWorkout(() -> workoutService
                                .withDeadlineAfter(WORKOUT_RPC_TIMEOUT_MS, TimeUnit.MILLISECONDS)
                                .releaseSlot(ReleaseSlotRequest.newBuilder()
                                        .setIdWorkout(idWorkout).build()));
                    } catch (RuntimeException compensationFailure) {
                        log.error("Failed to release workout {} after booking transaction rollback",
                                idWorkout, compensationFailure);
                    }
                }
            }
        });
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

    @Override
    @Transactional
    public Subscription createSubscription(Long clientId, Integer typeId, LocalDate startDate) {
        if (clientId == null || clientId <= 0 || typeId == null || typeId <= 0 || startDate == null) {
            throw new IllegalArgumentException("Client, subscription type, and start date are required");
        }
        if (subscriptionRepository.findFirstByIdClientAndStatusOrderByEndDateDesc(
                clientId, SubscriptionStatus.ACTIVE).isPresent()) {
            throw Status.FAILED_PRECONDITION
                    .withDescription("Client already has an active subscription")
                    .asRuntimeException();
        }
        LocalDate start = startDate;
        Subscription subscription = new Subscription();
        subscription.setIdClient(clientId);
        subscription.setSubscriptionTypeId(typeId);
        subscription.setStartDate(start);
        subscription.setEndDate(start.plusDays(90));
        subscription.setStatus(SubscriptionStatus.PENDING);
        subscription.setRemainingSessions(0);
        return subscriptionRepository.save(subscription);
    }

    @Override
    @Transactional
    public Subscription changeSubscriptionStatus(Long id, String status) {
        Subscription subscription = getSubscriptionById(id);
        SubscriptionStatus next;
        try {
            next = SubscriptionStatus.valueOf(status.toUpperCase(java.util.Locale.ROOT));
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException("Unsupported subscription status: " + status);
        }
        subscription.setStatus(next);
        if (next == SubscriptionStatus.ACTIVE && subscription.getRemainingSessions() == 0) {
            subscription.setRemainingSessions(12);
        }
        return subscriptionRepository.save(subscription);
    }

    @Override
    @Transactional
    public Payment registerPayment(Long clientId, BigDecimal amount, Long subscriptionId,
                                  LocalDateTime paymentDate) {
        if (clientId == null || clientId <= 0 || amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Client and positive payment amount are required");
        }
        Payment payment = new Payment();
        payment.setIdClient(clientId);
        payment.setAmount(amount);
        payment.setPaymentDate(paymentDate == null ? LocalDateTime.now() : paymentDate);
        payment.setStatus(PaymentStatus.CONFIRMED);
        if (subscriptionId != null && subscriptionId > 0) {
            Subscription subscription = getSubscriptionById(subscriptionId);
            if (!clientId.equals(subscription.getIdClient())) {
                throw Status.PERMISSION_DENIED.withDescription("Subscription belongs to another client")
                        .asRuntimeException();
            }
            payment.setSubscription(subscription);
            subscription.setStatus(SubscriptionStatus.ACTIVE);
            if (subscription.getRemainingSessions() == 0) subscription.setRemainingSessions(12);
            subscriptionRepository.save(subscription);
        }
        return paymentRepository.save(payment);
    }
}
