package spiring.exapmle.apigateway.dto;

import lombok.Data;

@Data
public class SubscriptionDto {
    private Long idSubscription;
    private Integer subscriptionTypeId;
    private String startDate;
    private String endDate;
    private String status;
    private Integer remainingSessions;
    private ClientDto client;
}