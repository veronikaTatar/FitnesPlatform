package spiring.exapmle.bookingservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import spiring.exapmle.bookingservice.models.Payment;
import spiring.exapmle.bookingservice.models.enums.PaymentStatus;
import java.time.LocalDateTime;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByIdClient(Long idClient);
    List<Payment> findByIdClientAndPaymentDateBetween(Long idClient, LocalDateTime from, LocalDateTime to);
    List<Payment> findByIdClientAndStatus(Long idClient, PaymentStatus status);
    List<Payment> findBySubscription_IdSubscription(Long idSubscription);
}