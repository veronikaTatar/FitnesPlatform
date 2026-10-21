package spiring.exapmle.apigateway.dto;

import lombok.Data;

@Data
public class BookingDto {
    private Long idBooking;
    private String status;
    private String bookingDate;
    private ClientDto client;
    private WorkoutDto workout;
}