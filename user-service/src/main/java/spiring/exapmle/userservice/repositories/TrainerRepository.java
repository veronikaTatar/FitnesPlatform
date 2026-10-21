package spiring.exapmle.userservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import spiring.exapmle.userservice.models.Trainer;
import java.util.List;
import java.util.Optional;

public interface TrainerRepository extends JpaRepository<Trainer, Long> {
    Optional<Trainer> findByUser_IdUser(Long idUser);
    List<Trainer> findBySpecializationContainingIgnoreCase(String specialization);
}