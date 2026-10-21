package spiring.exapmle.workoutservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import spiring.exapmle.workoutservice.models.Progress;
import java.time.LocalDate;
import java.util.List;

public interface ProgressRepository extends JpaRepository<Progress, Long> {
    List<Progress> findByIdClientOrderByDateDesc(Long idClient);
    List<Progress> findByIdClientAndDateBetween(Long idClient, LocalDate from, LocalDate to);
    List<Progress> findByIdBooking(Long idBooking);
}