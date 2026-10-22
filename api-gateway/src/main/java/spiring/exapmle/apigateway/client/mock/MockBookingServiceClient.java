package spiring.exapmle.apigateway.client.mock;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import spiring.exapmle.apigateway.client.BookingServiceClient;
import spiring.exapmle.apigateway.dto.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
@Primary
public class MockBookingServiceClient implements BookingServiceClient {

    private final List<BookingDto> bookings = new ArrayList<>();
    private final List<SubscriptionDto> subscriptions = new ArrayList<>();
    private final List<PaymentDto> payments = new ArrayList<>();

    /** Общие справочники — используются для наполнения вложенных полей client/workout */
    private final List<ClientDto> clients = new ArrayList<>();
    private final List<WorkoutDto> workouts = new ArrayList<>();
    private final List<SubscriptionDto> subscriptionsRef = new ArrayList<>();

    /** Счётчики для генерации новых идентификаторов в бизнес-операциях ЛР4 */
    private final AtomicLong bookingIdCounter      = new AtomicLong(100);
    private final AtomicLong subscriptionIdCounter = new AtomicLong(100);
    private final AtomicLong paymentIdCounter      = new AtomicLong(100);

    public MockBookingServiceClient() {
        // ==== Клиенты (только с id, для вложенных ссылок) ====
        clients.add(client(1L, "1995-03-15", "beginner"));
        clients.add(client(2L, "1992-07-22", "intermediate"));

        // ==== Тренировочные программы и занятия (для вложенных workout) ====
        TrainerDto trainer1 = trainer(1L, "Йога", 4.8f);
        TrainerDto trainer2 = trainer(2L, "Силовая", 4.6f);

        TrainingProgramDto program1 = program(1L, "Йога для начинающих", "Йога", "beginner", 60, trainer1);
        TrainingProgramDto program2 = program(2L, "Силовая тренировка", "Силовая", "advanced", 90, trainer2);
        TrainingProgramDto program3 = program(3L, "Утренняя растяжка", "Йога", "intermediate", 45, trainer1);

        workouts.add(workout(1L, "2026-10-05T18:00:00", 60, "PLANNED", 15, program1, trainer1));
        workouts.add(workout(2L, "2026-10-07T18:00:00", 60, "PLANNED", 15, program1, trainer1));
        workouts.add(workout(3L, "2026-10-06T19:00:00", 90, "PLANNED", 10, program2, trainer2));
        workouts.add(workout(4L, "2026-10-08T08:00:00", 45, "PLANNED", 20, program3, trainer1));
        workouts.add(workout(5L, "2026-10-01T19:00:00", 90, "COMPLETED", 10, program2, trainer2));

        // ==== Абонементы ====
        SubscriptionDto sub1 = subscription(1L, 1, "2026-09-01", "2026-12-01", "ACTIVE", 10, client(1L, null, null));
        SubscriptionDto sub2 = subscription(2L, 1, "2026-09-15", "2026-12-15", "ACTIVE", 8, client(2L, null, null));
        SubscriptionDto sub3 = subscription(3L, 2, "2026-06-01", "2026-09-01", "COMPLETED", 0, client(1L, null, null));
        subscriptions.add(sub1);
        subscriptions.add(sub2);
        subscriptions.add(sub3);

        // ==== Записи на занятия ====
        bookings.add(booking(1L, "ACTIVE", "2026-10-01T10:00:00",
                client(1L, null, null),
                workouts.get(0)));
        bookings.add(booking(2L, "ACTIVE",    "2026-10-02T10:00:00", client(1L, null, null), workouts.get(1)));
        bookings.add(booking(3L, "ACTIVE",    "2026-10-02T11:30:00", client(2L, null, null), workouts.get(2)));
        bookings.add(booking(4L, "CANCELLED", "2026-09-30T09:00:00", client(1L, null, null), workouts.get(4)));
        bookings.add(booking(5L, "ACTIVE",    "2026-10-03T12:00:00", client(2L, null, null), workouts.get(0)));

        // ==== Платежи ====
        payments.add(payment(1L, 150.00f, "2026-09-01T12:00:00", "CONFIRMED", client(1L, null, null), sub1));
        payments.add(payment(2L, 150.00f, "2026-09-15T12:30:00", "CONFIRMED", client(2L, null, null), sub2));
        payments.add(payment(3L,  25.00f, "2026-10-01T10:00:00", "CONFIRMED", client(1L, null, null), null));
        payments.add(payment(4L,  30.00f, "2026-10-02T11:30:00", "PENDING",   client(2L, null, null), null));
    }

    // ================== Реализация контракта ==================

    @Override
    public List<BookingDto> getBookings(Long clientId, Long workoutId) {
        return bookings.stream()
                .filter(b -> clientId == null
                        || (b.getClient() != null && clientId.equals(b.getClient().getIdClient())))
                .filter(b -> workoutId == null
                        || (b.getWorkout() != null && workoutId.equals(b.getWorkout().getIdWorkout())))
                .collect(Collectors.toList());
    }

    @Override
    public BookingDto getBooking(Long id) {
        return bookings.stream()
                .filter(b -> id.equals(b.getIdBooking()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Booking not found: " + id));
    }

    @Override
    public List<BookingDto> getBookingsByClient(Long clientId, String status) {
        return bookings.stream()
                .filter(b -> b.getClient() != null && clientId.equals(b.getClient().getIdClient()))
                .filter(b -> status == null || status.equals(b.getStatus()))
                .collect(Collectors.toList());
    }

    @Override
    public List<SubscriptionDto> getSubscriptionsByClient(Long clientId) {
        return subscriptions.stream()
                .filter(s -> s.getClient() != null && clientId.equals(s.getClient().getIdClient()))
                .collect(Collectors.toList());
    }

    @Override
    public SubscriptionDto getSubscription(Long id) {
        return subscriptions.stream()
                .filter(s -> id.equals(s.getIdSubscription()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Subscription not found: " + id));
    }

    @Override
    public List<PaymentDto> getPaymentsByClient(Long clientId, String dateFrom, String dateTo) {
        return payments.stream()
                .filter(p -> p.getClient() != null && clientId.equals(p.getClient().getIdClient()))
                .filter(p -> dateFrom == null || p.getPaymentDate().compareTo(dateFrom) >= 0)
                .filter(p -> dateTo == null || p.getPaymentDate().compareTo(dateTo) <= 0)
                .collect(Collectors.toList());
    }

    // ================== Вспомогательные фабрики ==================

    private static ClientDto client(Long id, String birthDate, String fitnessLevel) {
        ClientDto c = new ClientDto();
        c.setIdClient(id);
        c.setBirthDate(birthDate);
        c.setFitnessLevel(fitnessLevel);
        return c;
    }

    private static TrainerDto trainer(Long id, String spec, Float rating) {
        TrainerDto t = new TrainerDto();
        t.setIdTrainer(id);
        t.setSpecialization(spec);
        t.setRating(rating);
        return t;
    }

    private static TrainingProgramDto program(Long id, String name, String type,
                                              String difficulty, Integer duration, TrainerDto trainer) {
        TrainingProgramDto p = new TrainingProgramDto();
        p.setIdTrainingProgram(id);
        p.setName(name);
        p.setType(type);
        p.setDifficultyLevel(difficulty);
        p.setDuration(duration);
        p.setTrainer(trainer);
        return p;
    }

    private static WorkoutDto workout(Long id, String start, Integer duration,
                                      String status, Integer maxCapacity,
                                      TrainingProgramDto program, TrainerDto trainer) {
        WorkoutDto w = new WorkoutDto();
        w.setIdWorkout(id);
        w.setStartDatetime(start);
        w.setDuration(duration);
        w.setStatus(status);
        w.setMaxCapacity(maxCapacity);
        w.setTrainingProgram(program);
        w.setTrainer(trainer);
        return w;
    }

    private static SubscriptionDto subscription(Long id, Integer typeId, String start, String end,
                                                String status, Integer remaining, ClientDto client) {
        SubscriptionDto s = new SubscriptionDto();
        s.setIdSubscription(id);
        s.setSubscriptionTypeId(typeId);
        s.setStartDate(start);
        s.setEndDate(end);
        s.setStatus(status);
        s.setRemainingSessions(remaining);
        s.setClient(client);
        return s;
    }

    private static BookingDto booking(Long id, String status, String date,
                                      ClientDto client, WorkoutDto workout) {
        BookingDto b = new BookingDto();
        b.setIdBooking(id);
        b.setStatus(status);
        b.setBookingDate(date);
        b.setClient(client);
        b.setWorkout(workout);
        return b;
    }

    private static PaymentDto payment(Long id, Float amount, String date, String status,
                                      ClientDto client, SubscriptionDto subscription) {
        PaymentDto p = new PaymentDto();
        p.setIdPayment(id);
        p.setAmount(amount);
        p.setPaymentDate(date);
        p.setStatus(status);
        p.setClient(client);
        p.setSubscription(subscription);
        return p;
    }

    // ================== Бизнес-операции (ЛР4) ==================

    @Override
    public BookingDto bookWorkout(Long clientId, Long workoutId) {
        WorkoutDto workout = workouts.stream()
                .filter(w -> workoutId.equals(w.getIdWorkout()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(
                        "Workout not found: " + workoutId));

        if (!"PLANNED".equals(workout.getStatus())) {
            throw new RuntimeException(
                    "Workout is not available for booking: " + workoutId);
        }

        long bookedCount = bookings.stream()
                .filter(b -> b.getWorkout() != null
                        && workoutId.equals(b.getWorkout().getIdWorkout())
                        && "ACTIVE".equals(b.getStatus()))
                .count();
        if (bookedCount >= workout.getMaxCapacity()) {
            throw new RuntimeException(
                    "No free slots for workout: " + workoutId);
        }

        SubscriptionDto activeSubscription = subscriptions.stream()
                .filter(s -> s.getClient() != null
                        && clientId.equals(s.getClient().getIdClient())
                        && "ACTIVE".equals(s.getStatus()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(
                        "No active subscription for client: " + clientId));

        if (activeSubscription.getRemainingSessions() <= 0) {
            throw new RuntimeException(
                    "No remaining sessions in subscription");
        }

        activeSubscription.setRemainingSessions(
                activeSubscription.getRemainingSessions() - 1);

        BookingDto booking = new BookingDto();
        booking.setIdBooking(bookingIdCounter.getAndIncrement());
        booking.setStatus("ACTIVE");
        booking.setBookingDate(java.time.LocalDateTime.now().toString());
        booking.setClient(activeSubscription.getClient());
        booking.setWorkout(workout);
        bookings.add(booking);
        return booking;
    }

    @Override
    public BookingDto cancelBooking(Long clientId, Long bookingId) {
        BookingDto booking = bookings.stream()
                .filter(b -> bookingId.equals(b.getIdBooking()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(
                        "Booking not found: " + bookingId));

        if (!"ACTIVE".equals(booking.getStatus())) {
            throw new RuntimeException(
                    "Only active bookings can be cancelled");
        }
        if (!clientId.equals(booking.getClient().getIdClient())) {
            throw new RuntimeException("Booking does not belong to authenticated client");
        }

        booking.setStatus("CANCELLED");
        subscriptions.stream()
                .filter(s -> s.getClient() != null
                        && clientId.equals(s.getClient().getIdClient())
                        && "ACTIVE".equals(s.getStatus()))
                .findFirst()
                .ifPresent(s -> s.setRemainingSessions(
                        s.getRemainingSessions() + 1));

        return booking;
    }

    @Override
    public SubscriptionDto createSubscription(Long clientId, CreateSubscriptionInputDto input) {
        if (input.getSubscriptionTypeId() == null) {
            throw new RuntimeException("Subscription type is required");
        }
        if (input.getStartDate() == null) {
            throw new RuntimeException("Start date is required");
        }

        // Проверка: у клиента не должно быть активного абонемента
        boolean hasActive = subscriptions.stream()
                .anyMatch(s -> s.getClient() != null
                        && clientId.equals(s.getClient().getIdClient())
                        && "ACTIVE".equals(s.getStatus()));
        if (hasActive) {
            throw new RuntimeException(
                    "Client already has an active subscription");
        }

        SubscriptionDto subscription = new SubscriptionDto();
        subscription.setIdSubscription(subscriptionIdCounter.getAndIncrement());
        subscription.setSubscriptionTypeId(input.getSubscriptionTypeId());
        subscription.setStartDate(input.getStartDate());
        subscription.setEndDate(java.time.LocalDate
                .parse(input.getStartDate()).plusDays(90).toString());
        subscription.setStatus("PENDING");
        subscription.setRemainingSessions(0); // заполнится после оплаты
        subscription.setClient(clients.stream()
                .filter(c -> clientId.equals(c.getIdClient()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(
                        "Client not found: " + clientId)));

        subscriptions.add(subscription);
        return subscription;
    }

    @Override
    public SubscriptionDto changeSubscriptionStatus(Long id, String status) {
        SubscriptionDto subscription = subscriptions.stream()
                .filter(s -> id.equals(s.getIdSubscription()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(
                        "Subscription not found: " + id));

        java.util.Set<String> allowed =
                java.util.Set.of("PENDING", "ACTIVE", "FROZEN", "COMPLETED");
        if (!allowed.contains(status)) {
            throw new RuntimeException("Invalid subscription status: " + status);
        }

        subscription.setStatus(status);
        return subscription;
    }

    @Override
    public PaymentDto registerPayment(RegisterPaymentInputDto input) {
        if (input.getAmount() == null || input.getAmount() <= 0) {
            throw new RuntimeException("Amount must be positive");
        }

        Long clientId = 1L; // в ЛР5 — из JWT

        PaymentDto payment = new PaymentDto();
        payment.setIdPayment(paymentIdCounter.getAndIncrement());
        payment.setAmount(input.getAmount());
        payment.setPaymentDate(input.getPaymentDate() != null
                ? input.getPaymentDate()
                : java.time.LocalDateTime.now().toString());
        payment.setStatus("CONFIRMED");
        payment.setClient(clients.stream()
                .filter(c -> clientId.equals(c.getIdClient()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(
                        "Client not found: " + clientId)));

        // Если указан абонемент — активируем его
        if (input.getIdSubscription() != null) {
            SubscriptionDto subscription = subscriptions.stream()
                    .filter(s -> input.getIdSubscription().equals(s.getIdSubscription()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException(
                            "Subscription not found: " + input.getIdSubscription()));

            subscription.setStatus("ACTIVE");
            subscription.setRemainingSessions(12);
            payment.setSubscription(subscription);
        }

        payments.add(payment);
        return payment;
    }


// ЛР4 Фильтрация записей

    @Override
    public List<BookingDto> getBookingsByClient(
            Long clientId, String status,
            String sortBy, String sortOrder) {

        List<BookingDto> result = bookings.stream()
                .filter(b -> b.getClient() != null
                        && clientId.equals(b.getClient().getIdClient()))
                .collect(Collectors.toList());

        if (status != null) {
            result = result.stream()
                    .filter(b -> status.equals(b.getStatus()))
                    .collect(Collectors.toList());
        }

        return sortBookings(result, sortBy, sortOrder);
    }

   //ЛР 4 Сортировка
    private List<BookingDto> sortBookings(List<BookingDto> list,
                                          String sortBy, String sortOrder) {
        if (sortBy == null) return list;

        Comparator<BookingDto> comparator = switch (sortBy) {
            case "bookingDate" -> Comparator.comparing(BookingDto::getBookingDate);
            case "status"      -> Comparator.comparing(BookingDto::getStatus);
            case "id"          -> Comparator.comparing(BookingDto::getIdBooking);
            default            -> Comparator.comparing(BookingDto::getIdBooking);
        };

        if ("DESC".equalsIgnoreCase(sortOrder)) {
            comparator = comparator.reversed();
        }

        list.sort(comparator);
        return list;
    }





}
