package spiring.exapmle.analyticsservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import spiring.exapmle.analyticsservice.models.AttendanceStats;
import java.time.LocalDate;
import java.util.List;

public interface AttendanceStatsRepository extends JpaRepository<AttendanceStats, Long> {
    List<AttendanceStats> findByDateBetweenOrderByDateAsc(LocalDate from, LocalDate to);
    List<AttendanceStats> findByDate(LocalDate date);
}