package spiring.exapmle.apigateway.dto;

import lombok.Data;

@Data
public class RegisterPaymentInputDto {
    private Float amount;
    private Long idSubscription;
    private String paymentDate;
}