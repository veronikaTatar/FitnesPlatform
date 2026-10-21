package spiring.exapmle.bookingservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import spiring.exapmle.bookingservice.models.Subscription;
import spiring.exapmle.bookingservice.models.enums.SubscriptionStatus;
import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    List<Subscription> findByIdClient(Long idClient);

    List<Subscription> findByIdClientAndStatus(Long idClient, SubscriptionStatus status);

    Optional<Subscription> findFirstByIdClientAndStatusOrderByEndDateDesc(
            Long idClient, SubscriptionStatus status);
}