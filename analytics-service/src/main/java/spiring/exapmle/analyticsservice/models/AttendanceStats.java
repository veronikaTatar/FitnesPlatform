package spiring.exapmle.analyticsservice.models;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "attendancestats")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class AttendanceStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idStat")
    private Long idStat;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "workout_count", nullable = false)
    private Integer workoutCount;

    @Column(name = "booking_count", nullable = false)
    private Integer bookingCount;

    @Column(name = "attendance_count", nullable = false)
    private Integer attendanceCount;
}