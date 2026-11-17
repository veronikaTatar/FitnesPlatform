package spiring.exapmle.bookingservice.controllers;

import com.fitness.booking.grpc.*;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.transaction.annotation.Transactional;
import spiring.exapmle.bookingservice.models.Booking;
import spiring.exapmle.bookingservice.models.Payment;
import spiring.exapmle.bookingservice.models.Subscription;
import spiring.exapmle.bookingservice.models.enums.BookingStatus;
import spiring.exapmle.bookingservice.models.enums.PaymentStatus;
import spiring.exapmle.bookingservice.services.BookingService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

@GrpcService
public class BookingGrpcController extends BookingServiceGrpc.BookingServiceImplBase {
    private final BookingService bookingService;

    public BookingGrpcController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @Override
    @Transactional(readOnly = true)
    public void getBooking(GetBookingRequest request, StreamObserver<BookingResponse> observer) {
        respond(observer, () -> toBooking(bookingService.getBookingById(request.getIdBooking())));
    }

    @Override
    @Transactional(readOnly = true)
    public void getBookings(ListBookingsRequest request,
                            StreamObserver<BookingListResponse> observer) {
        respond(observer, () -> BookingListResponse.newBuilder()
                .addAllBookings(bookingService.getBookings(
                                request.getIdClient() == 0 ? null : request.getIdClient(),
                                request.getIdWorkout() == 0 ? null : request.getIdWorkout())
                        .stream().map(BookingGrpcController::toBooking).toList())
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public void getClientBookings(GetClientBookingsRequest request,
                                  StreamObserver<BookingListResponse> observer) {
        respond(observer, () -> {
            String status = request.getStatus().isBlank() ? null
                    : request.getStatus().toUpperCase(Locale.ROOT);
            List<Booking> bookings = status == null
                    ? bookingService.getBookingsByClient(request.getIdClient())
                    : bookingService.getBookingsByClientAndStatus(request.getIdClient(), status);
            Comparator<Booking> comparator = switch (request.getSortBy()) {
                case "bookingDate" -> Comparator.comparing(Booking::getBookingDate);
                case "status" -> Comparator.comparing(b -> b.getStatus().name());
                case "id" -> Comparator.comparing(Booking::getIdBooking);
                default -> null;
            };
            if (comparator != null) {
                if ("DESC".equalsIgnoreCase(request.getSortOrder())) comparator = comparator.reversed();
                bookings.sort(comparator);
            }
            return BookingListResponse.newBuilder().addAllBookings(
                    bookings.stream().map(BookingGrpcController::toBooking).toList()).build();
        });
    }

    @Override
    @Transactional(readOnly = true)
    public void getSubscription(GetSubscriptionRequest request,
                                StreamObserver<SubscriptionResponse> observer) {
        respond(observer, () -> toSubscription(
                bookingService.getSubscriptionById(request.getIdSubscription())));
    }

    @Override
    @Transactional(readOnly = true)
    public void getClientSubscriptions(GetClientSubscriptionsRequest request,
                                       StreamObserver<SubscriptionListResponse> observer) {
        respond(observer, () -> SubscriptionListResponse.newBuilder()
                .addAllSubscriptions(bookingService.getSubscriptionsByClient(request.getIdClient())
                        .stream().map(BookingGrpcController::toSubscription).toList())
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public void getActiveSubscription(GetActiveSubscriptionRequest request,
                                      StreamObserver<SubscriptionResponse> observer) {
        respond(observer, () -> toSubscription(
                bookingService.getActiveSubscription(request.getIdClient())));
    }

    @Override
    @Transactional(readOnly = true)
    public void getPayment(GetPaymentRequest request, StreamObserver<PaymentResponse> observer) {
        respond(observer, () -> toPayment(bookingService.getPaymentById(request.getIdPayment())));
    }

    @Override
    @Transactional(readOnly = true)
    public void getClientPayments(GetClientPaymentsRequest request,
                                  StreamObserver<PaymentListResponse> observer) {
        respond(observer, () -> {
            LocalDateTime from = parseFrom(request.getDateFrom());
            LocalDateTime to = parseTo(request.getDateTo());
            List<Payment> payments;
            if (from == null && to == null) {
                payments = bookingService.getPaymentsByClient(request.getIdClient());
            } else {
                if (from == null) from = LocalDate.of(1, 1, 1).atStartOfDay();
                if (to == null) to = LocalDate.of(9999, 12, 31).atTime(LocalTime.MAX);
                payments = bookingService.getPaymentsByClientAndPeriod(
                        request.getIdClient(), from, to);
            }
            return PaymentListResponse.newBuilder().addAllPayments(
                    payments.stream().map(BookingGrpcController::toPayment).toList()).build();
        });
    }

    @Override
    public void bookWorkout(BookWorkoutRequest request, StreamObserver<BookingResponse> observer) {
        respond(observer, () -> toBooking(bookingService.bookWorkout(
                request.getIdClient(), request.getIdWorkout())));
    }

    @Override
    public void cancelBooking(CancelBookingRequest request,
                              StreamObserver<BookingResponse> observer) {
        respond(observer, () -> toBooking(bookingService.cancelBooking(
                request.getIdClient(), request.getIdBooking())));
    }

    @Override
    public void createSubscription(CreateSubscriptionRequest request,
                                   StreamObserver<SubscriptionResponse> observer) {
        respond(observer, () -> toSubscription(bookingService.createSubscription(
                request.getIdClient(), request.getSubscriptionTypeId(),
                request.getStartDate().isBlank() ? null : LocalDate.parse(request.getStartDate()))));
    }

    @Override
    public void changeSubscriptionStatus(ChangeSubscriptionStatusRequest request,
                                         StreamObserver<SubscriptionResponse> observer) {
        respond(observer, () -> toSubscription(bookingService.changeSubscriptionStatus(
                request.getIdSubscription(), request.getStatus())));
    }

    @Override
    public void registerPayment(RegisterPaymentRequest request,
                                StreamObserver<PaymentResponse> observer) {
        respond(observer, () -> toPayment(bookingService.registerPayment(
                request.getIdClient(), BigDecimal.valueOf(request.getAmount()),
                request.getIdSubscription() == 0 ? null : request.getIdSubscription(),
                request.getPaymentDate().isBlank() ? null
                        : LocalDateTime.parse(request.getPaymentDate()))));
    }

    private static BookingResponse toBooking(Booking booking) {
        return BookingResponse.newBuilder()
                .setIdBooking(booking.getIdBooking())
                .setIdClient(booking.getIdClient())
                .setIdWorkout(booking.getIdWorkout())
                .setStatus(booking.getStatus() == BookingStatus.ACTIVE
                        ? com.fitness.booking.grpc.BookingStatus.ACTIVE
                        : com.fitness.booking.grpc.BookingStatus.CANCELLED)
                .setBookingDate(booking.getBookingDate().toString())
                .build();
    }

    private static SubscriptionResponse toSubscription(Subscription subscription) {
        return SubscriptionResponse.newBuilder()
                .setIdSubscription(subscription.getIdSubscription())
                .setIdClient(subscription.getIdClient())
                .setStatus(subscription.getStatus().name())
                .setRemainingSessions(subscription.getRemainingSessions())
                .setEndDate(subscription.getEndDate().toString())
                .setSubscriptionTypeId(subscription.getSubscriptionTypeId())
                .setStartDate(subscription.getStartDate().toString())
                .build();
    }

    private static PaymentResponse toPayment(Payment payment) {
        return PaymentResponse.newBuilder()
                .setIdPayment(payment.getIdPayment())
                .setIdClient(payment.getIdClient())
                .setAmount(payment.getAmount().doubleValue())
                .setStatus(switch (payment.getStatus()) {
                    case PENDING -> com.fitness.booking.grpc.PaymentStatus.PENDING;
                    case CONFIRMED -> com.fitness.booking.grpc.PaymentStatus.CONFIRMED;
                    case FAILED -> com.fitness.booking.grpc.PaymentStatus.FAILED;
                })
                .setPaymentDate(payment.getPaymentDate().toString())
                .build();
    }

    private static LocalDateTime parseFrom(String value) {
        if (value == null || value.isBlank()) return null;
        return value.length() == 10 ? LocalDate.parse(value).atStartOfDay()
                : LocalDateTime.parse(value);
    }

    private static LocalDateTime parseTo(String value) {
        if (value == null || value.isBlank()) return null;
        return value.length() == 10 ? LocalDate.parse(value).atTime(LocalTime.MAX)
                : LocalDateTime.parse(value);
    }

    private static <T> void respond(StreamObserver<T> observer, Supplier<T> action) {
        try {
            observer.onNext(action.get());
            observer.onCompleted();
        } catch (StatusRuntimeException exception) {
            observer.onError(exception);
        } catch (IllegalArgumentException exception) {
            observer.onError(Status.INVALID_ARGUMENT
                    .withDescription(exception.getMessage()).asRuntimeException());
        } catch (IllegalStateException exception) {
            observer.onError(Status.FAILED_PRECONDITION
                    .withDescription(exception.getMessage()).asRuntimeException());
        } catch (RuntimeException exception) {
            Status status = exception.getMessage() != null
                    && exception.getMessage().toLowerCase(Locale.ROOT).contains("not found")
                    ? Status.NOT_FOUND : Status.INTERNAL;
            observer.onError(status.withDescription(exception.getMessage()).asRuntimeException());
        }
    }
}
