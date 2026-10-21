package spiring.exapmle.apigateway.client.mock;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import spiring.exapmle.apigateway.client.AnalyticsServiceClient;
import spiring.exapmle.apigateway.dto.AggregatedStatsDto;
import spiring.exapmle.apigateway.dto.AttendanceStatsDto;
import spiring.exapmle.apigateway.dto.ProgramPopularityDto;
import spiring.exapmle.apigateway.dto.RevenueStatsDto;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Primary
public class MockAnalyticsServiceClient implements AnalyticsServiceClient {

    private final List<AttendanceStatsDto> attendanceStats = new ArrayList<>();
    private final List<RevenueStatsDto> revenueStats = new ArrayList<>();
    private final List<ProgramPopularityDto> programPopularity = new ArrayList<>();

    public MockAnalyticsServiceClient() {
        attendanceStats.add(attendance("2026-09-27", 3, 5, 4));
        attendanceStats.add(attendance("2026-09-28", 2, 3, 3));
        attendanceStats.add(attendance("2026-09-29", 4, 6, 5));
        attendanceStats.add(attendance("2026-09-30", 3, 4, 4));
        attendanceStats.add(attendance("2026-10-01", 2, 3, 2));
        attendanceStats.add(attendance("2026-10-02", 1, 2, 2));
        attendanceStats.add(attendance("2026-10-03", 3, 5, 4));

        revenueStats.add(revenue("2026-09-27", 425.00f, 300.00f, 3));
        revenueStats.add(revenue("2026-09-28", 150.00f, 150.00f, 1));
        revenueStats.add(revenue("2026-09-29", 310.00f, 150.00f, 2));
        revenueStats.add(revenue("2026-09-30", 175.00f, 150.00f, 1));
        revenueStats.add(revenue("2026-10-01", 175.00f, 150.00f, 1));
        revenueStats.add(revenue("2026-10-02", 150.00f, 150.00f, 1));
        revenueStats.add(revenue("2026-10-03",  30.00f,   0.00f, 1));

        programPopularity.add(popularity(1L, 12, "week"));
        programPopularity.add(popularity(2L,  8, "week"));
        programPopularity.add(popularity(3L,  5, "week"));
        programPopularity.add(popularity(1L, 45, "month"));
        programPopularity.add(popularity(2L, 32, "month"));
        programPopularity.add(popularity(3L, 18, "month"));
    }

    @Override
    public List<AttendanceStatsDto> getAttendanceStats(String dateFrom, String dateTo) {
        return attendanceStats.stream()
                .filter(s -> dateFrom == null || s.getDate().compareTo(dateFrom) >= 0)
                .filter(s -> dateTo == null || s.getDate().compareTo(dateTo) <= 0)
                .collect(Collectors.toList());
    }

    @Override
    public List<RevenueStatsDto> getRevenueStats(String dateFrom, String dateTo) {
        return revenueStats.stream()
                .filter(s -> dateFrom == null || s.getDate().compareTo(dateFrom) >= 0)
                .filter(s -> dateTo == null || s.getDate().compareTo(dateTo) <= 0)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProgramPopularityDto> getProgramPopularity(String period) {
        return programPopularity.stream()
                .filter(p -> period == null || period.equals(p.getPeriod()))
                .collect(Collectors.toList());
    }

    private static AttendanceStatsDto attendance(String date, Integer workoutCount,
                                                 Integer bookingCount, Integer attendanceCount) {
        AttendanceStatsDto s = new AttendanceStatsDto();
        s.setDate(date);
        s.setWorkoutCount(workoutCount);
        s.setBookingCount(bookingCount);
        s.setAttendanceCount(attendanceCount);
        return s;
    }

    private static RevenueStatsDto revenue(String date, Float total, Float subscription, Integer count) {
        RevenueStatsDto s = new RevenueStatsDto();
        s.setDate(date);
        s.setTotalRevenue(total);
        s.setSubscriptionRevenue(subscription);
        s.setPaymentCount(count);
        return s;
    }

    private static ProgramPopularityDto popularity(Long programId, Integer bookingCount, String period) {
        ProgramPopularityDto p = new ProgramPopularityDto();
        p.setIdTrainingProgram(programId);
        p.setBookingCount(bookingCount);
        p.setPeriod(period);
        return p;
    }


    // ================== Агрегирующие операции (ЛР4) ==================

    @Override
    public AggregatedStatsDto getAggregatedStats(String dateFrom, String dateTo) {
        List<AttendanceStatsDto> attendance = getAttendanceStats(dateFrom, dateTo);
        List<RevenueStatsDto>    revenue    = getRevenueStats(dateFrom, dateTo);

        AggregatedStatsDto result = new AggregatedStatsDto();

        // --- Посещаемость ---
        int totalBookings = attendance.stream()
                .mapToInt(AttendanceStatsDto::getBookingCount)
                .sum();
        int totalAttendance = attendance.stream()
                .mapToInt(AttendanceStatsDto::getAttendanceCount)
                .sum();
        double averageAttendance = attendance.stream()
                .mapToInt(AttendanceStatsDto::getAttendanceCount)
                .average()
                .orElse(0.0);

        result.setTotalBookings(totalBookings);
        result.setTotalAttendance(totalAttendance);
        result.setAverageAttendance((float) averageAttendance);

        // --- Выручка ---
        double totalRevenue = revenue.stream()
                .mapToDouble(RevenueStatsDto::getTotalRevenue)
                .sum();
        double subscriptionRevenue = revenue.stream()
                .mapToDouble(RevenueStatsDto::getSubscriptionRevenue)
                .sum();
        int paymentCount = revenue.stream()
                .mapToInt(RevenueStatsDto::getPaymentCount)
                .sum();

        result.setTotalRevenue((float) totalRevenue);
        result.setSubscriptionRevenue((float) subscriptionRevenue);
        result.setPaymentCount(paymentCount);

        // --- Топ-5 программ за месяц ---
        List<ProgramPopularityDto> top = getProgramPopularity("month").stream()
                .sorted(Comparator.comparing(
                        ProgramPopularityDto::getBookingCount).reversed())
                .limit(5)
                .collect(Collectors.toList());

        result.setTopPrograms(top);

        return result;
    }
}