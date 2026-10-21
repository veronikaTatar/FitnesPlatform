package spiring.exapmle.apigateway.dto;

import lombok.Data;

@Data
public class RevenueStatsDto {
    private String date;
    private Float totalRevenue;
    private Float subscriptionRevenue;
    private Integer paymentCount;
}