package spiring.exapmle.analyticsservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import spiring.exapmle.analyticsservice.models.ProcessedBookingEvent;

public interface ProcessedBookingEventRepository extends JpaRepository<ProcessedBookingEvent, String> {
}
