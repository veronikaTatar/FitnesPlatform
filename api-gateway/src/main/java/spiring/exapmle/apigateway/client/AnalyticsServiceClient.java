package spiring.exapmle.apigateway.client;

import spiring.exapmle.apigateway.dto.AggregatedStatsDto;
import spiring.exapmle.apigateway.dto.AttendanceStatsDto;
import spiring.exapmle.apigateway.dto.ProgramPopularityDto;
import spiring.exapmle.apigateway.dto.RevenueStatsDto;
import java.util.List;

public interface AnalyticsServiceClient {
    List<AttendanceStatsDto> getAttendanceStats(String dateFrom, String dateTo);
    List<RevenueStatsDto> getRevenueStats(String dateFrom, String dateTo);
    List<ProgramPopularityDto> getProgramPopularity(String period);

    // ЛР4 агрегация
    AggregatedStatsDto getAggregatedStats(String dateFrom, String dateTo);
}