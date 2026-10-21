package spiring.exapmle.userservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import spiring.exapmle.userservice.models.Client;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<Client, Long> {
    Optional<Client> findByUser_IdUser(Long idUser);
}