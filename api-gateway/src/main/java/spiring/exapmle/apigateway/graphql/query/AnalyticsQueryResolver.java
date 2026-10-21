package spiring.exapmle.apigateway.graphql.query;

import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import spiring.exapmle.apigateway.client.AnalyticsServiceClient;
import spiring.exapmle.apigateway.dto.AggregatedStatsDto;
import spiring.exapmle.apigateway.dto.AttendanceStatsDto;
import spiring.exapmle.apigateway.dto.ProgramPopularityDto;
import spiring.exapmle.apigateway.dto.RevenueStatsDto;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class AnalyticsQueryResolver {

    private final AnalyticsServiceClient analyticsServiceClient;

    @QueryMapping
    public List<AttendanceStatsDto> attendanceStats(@Argument String dateFrom,
                                                    @Argument String dateTo) {
        return analyticsServiceClient.getAttendanceStats(dateFrom, dateTo);
    }

    @QueryMapping
    public List<RevenueStatsDto> revenueStats(@Argument String dateFrom,
                                              @Argument String dateTo) {
        return analyticsServiceClient.getRevenueStats(dateFrom, dateTo);
    }

    @QueryMapping
    public List<ProgramPopularityDto> programPopularity(@Argument String period) {
        return analyticsServiceClient.getProgramPopularity(period);
    }

    @QueryMapping
    public AggregatedStatsDto aggregatedStats(@Argument String dateFrom,
                                              @Argument String dateTo) {
        return analyticsServiceClient.getAggregatedStats(dateFrom, dateTo);
    }
}