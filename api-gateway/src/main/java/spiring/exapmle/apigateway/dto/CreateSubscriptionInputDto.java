package spiring.exapmle.apigateway.dto;

import lombok.Data;

@Data
public class CreateSubscriptionInputDto {
    private Integer subscriptionTypeId;
    private String startDate;
}