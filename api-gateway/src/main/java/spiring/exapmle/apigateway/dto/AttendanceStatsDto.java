package spiring.exapmle.apigateway.dto;

import lombok.Data;

@Data
public class AttendanceStatsDto {
    private String date;
    private Integer workoutCount;
    private Integer bookingCount;
    private Integer attendanceCount;
}