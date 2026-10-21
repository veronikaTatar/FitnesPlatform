package spiring.exapmle.apigateway.dto;

import lombok.Data;

@Data
public class NotificationDto {
    private Long idNotification;
    private String text;
    private String date;
    private Boolean isRead;
    private String type;
}