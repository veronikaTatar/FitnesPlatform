package spiring.exapmle.analyticsservice.controllers;

import com.fitness.analytics.grpc.*;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.transaction.annotation.Transactional;
import spiring.exapmle.analyticsservice.models.AttendanceStats;
import spiring.exapmle.analyticsservice.models.ProgramPopularity;
import spiring.exapmle.analyticsservice.models.RevenueStats;
import spiring.exapmle.analyticsservice.repositories.AttendanceStatsRepository;
import spiring.exapmle.analyticsservice.repositories.ProgramPopularityRepository;
import spiring.exapmle.analyticsservice.repositories.RevenueStatsRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

@GrpcService
public class AnalyticsGrpcController extends AnalyticsServiceGrpc.AnalyticsServiceImplBase {
    private final AttendanceStatsRepository attendanceRepository;
    private final RevenueStatsRepository revenueRepository;
    private final ProgramPopularityRepository popularityRepository;

    public AnalyticsGrpcController(AttendanceStatsRepository attendanceRepository,
                                   RevenueStatsRepository revenueRepository,
                                   ProgramPopularityRepository popularityRepository) {
        this.attendanceRepository = attendanceRepository;
        this.revenueRepository = revenueRepository;
        this.popularityRepository = popularityRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public void getAttendanceStats(StatsPeriodRequest request,
                                   StreamObserver<AttendanceStatsResponse> observer) {
        respond(observer, () -> AttendanceStatsResponse.newBuilder()
                .addAllItems(attendanceRepository
                        .findByDateBetweenOrderByDateAsc(parseDate(request.getDateFrom()),
                                parseDate(request.getDateTo()))
                        .stream().map(AnalyticsGrpcController::toAttendance).toList())
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public void getRevenueStats(StatsPeriodRequest request,
                                StreamObserver<RevenueStatsResponse> observer) {
        respond(observer, () -> RevenueStatsResponse.newBuilder()
                .addAllItems(revenueRepository
                        .findByDateBetweenOrderByDateAsc(parseDate(request.getDateFrom()),
                                parseDate(request.getDateTo()))
                        .stream().map(AnalyticsGrpcController::toRevenue).toList())
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public void getProgramPopularity(PopularityRequest request,
                                     StreamObserver<PopularityResponse> observer) {
        respond(observer, () -> PopularityResponse.newBuilder()
                .addAllItems(popularityRepository
                        .findByPeriodOrderByBookingCountDesc(request.getPeriod())
                        .stream().map(AnalyticsGrpcController::toPopularity).toList())
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public void getAggregatedStats(StatsPeriodRequest request,
                                   StreamObserver<AggregatedStatsResponse> observer) {
        respond(observer, () -> {
            List<AttendanceStats> attendance = attendanceRepository
                    .findByDateBetweenOrderByDateAsc(parseDate(request.getDateFrom()),
                            parseDate(request.getDateTo()));
            List<RevenueStats> revenue = revenueRepository
                    .findByDateBetweenOrderByDateAsc(parseDate(request.getDateFrom()),
                            parseDate(request.getDateTo()));
            int bookingCount = attendance.stream()
                    .mapToInt(AttendanceStats::getBookingCount).sum();
            int attendanceCount = attendance.stream()
                    .mapToInt(AttendanceStats::getAttendanceCount).sum();
            double averageAttendance = attendance.stream()
                    .mapToInt(AttendanceStats::getAttendanceCount).average().orElse(0.0);
            double totalRevenue = revenue.stream()
                    .map(RevenueStats::getTotalRevenue).mapToDouble(BigDecimal::doubleValue).sum();
            double subscriptionRevenue = revenue.stream()
                    .map(RevenueStats::getSubscriptionRevenue)
                    .mapToDouble(BigDecimal::doubleValue).sum();
            int paymentCount = revenue.stream()
                    .mapToInt(RevenueStats::getPaymentCount).sum();
            List<PopularityItem> top = popularityRepository
                    .findByPeriodOrderByBookingCountDesc("month").stream()
                    .sorted(Comparator.comparing(ProgramPopularity::getBookingCount).reversed())
                    .limit(5).map(AnalyticsGrpcController::toPopularity).toList();
            return AggregatedStatsResponse.newBuilder()
                    .setTotalBookings(bookingCount)
                    .setTotalAttendance(attendanceCount)
                    .setAverageAttendance(averageAttendance)
                    .setTotalRevenue(totalRevenue)
                    .setSubscriptionRevenue(subscriptionRevenue)
                    .setPaymentCount(paymentCount)
                    .addAllTopPrograms(top)
                    .build();
        });
    }

    private static AttendanceStatItem toAttendance(AttendanceStats value) {
        return AttendanceStatItem.newBuilder()
                .setDate(value.getDate().toString())
                .setWorkoutCount(value.getWorkoutCount())
                .setBookingCount(value.getBookingCount())
                .setAttendanceCount(value.getAttendanceCount())
                .build();
    }

    private static RevenueStatItem toRevenue(RevenueStats value) {
        return RevenueStatItem.newBuilder()
                .setDate(value.getDate().toString())
                .setTotalRevenue(value.getTotalRevenue().doubleValue())
                .setSubscriptionRevenue(value.getSubscriptionRevenue().doubleValue())
                .setPaymentCount(value.getPaymentCount())
                .build();
    }

    private static PopularityItem toPopularity(ProgramPopularity value) {
        return PopularityItem.newBuilder()
                .setIdTrainingProgram(value.getIdTrainingProgram())
                .setProgramName("Program " + value.getIdTrainingProgram())
                .setBookingCount(value.getBookingCount())
                .setPeriod(value.getPeriod())
                .build();
    }

    private static LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Both date_from and date_to are required");
        }
        return LocalDate.parse(value);
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
        } catch (RuntimeException exception) {
            observer.onError(Status.INTERNAL
                    .withDescription(exception.getMessage()).asRuntimeException());
        }
    }
}
