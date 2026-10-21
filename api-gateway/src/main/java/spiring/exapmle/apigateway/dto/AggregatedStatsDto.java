package spiring.exapmle.apigateway.dto;

import lombok.Data;
import java.util.List;

@Data
public class AggregatedStatsDto {
    private Integer totalBookings;        // всего бронирований за период
    private Integer totalAttendance;      // всего подтверждённых посещений
    private Float   totalRevenue;         // суммарная выручка
    private Float   subscriptionRevenue;  // выручка с абонементов
    private Integer paymentCount;         // количество платежей
    private Float   averageAttendance;    // среднее посещений в день
    private List<ProgramPopularityDto> topPrograms; // топ-5 популярных программ
}