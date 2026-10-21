package spiring.exapmle.bookingservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import spiring.exapmle.bookingservice.models.Booking;
import spiring.exapmle.bookingservice.models.enums.BookingStatus;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByIdClient(Long idClient);
    List<Booking> findByIdClientAndStatus(Long idClient, BookingStatus status);
    List<Booking> findByIdWorkout(Long idWorkout);
    List<Booking> findByIdWorkoutAndStatus(Long idWorkout, BookingStatus status);
    long countByIdWorkoutAndStatus(Long idWorkout, BookingStatus status);
}