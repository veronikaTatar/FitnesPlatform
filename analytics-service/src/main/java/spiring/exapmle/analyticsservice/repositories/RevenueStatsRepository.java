package spiring.exapmle.analyticsservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import spiring.exapmle.analyticsservice.models.RevenueStats;
import java.time.LocalDate;
import java.util.List;

public interface RevenueStatsRepository extends JpaRepository<RevenueStats, Long> {
    List<RevenueStats> findByDateBetweenOrderByDateAsc(LocalDate from, LocalDate to);
    List<RevenueStats> findByDate(LocalDate date);
}