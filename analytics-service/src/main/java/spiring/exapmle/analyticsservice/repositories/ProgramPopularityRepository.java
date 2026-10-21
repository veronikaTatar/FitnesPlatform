package spiring.exapmle.analyticsservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import spiring.exapmle.analyticsservice.models.ProgramPopularity;
import java.util.List;

public interface ProgramPopularityRepository extends JpaRepository<ProgramPopularity, Long> {
    List<ProgramPopularity> findByPeriodOrderByBookingCountDesc(String period);
    List<ProgramPopularity> findByIdTrainingProgram(Long idTrainingProgram);
}