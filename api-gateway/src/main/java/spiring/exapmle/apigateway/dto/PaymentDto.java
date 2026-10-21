package spiring.exapmle.apigateway.dto;

import lombok.Data;

@Data
public class PaymentDto {
    private Long idPayment;
    private Float amount;
    private String paymentDate;
    private String status;
    private ClientDto client;
    private SubscriptionDto subscription;
}