package spiring.exapmle.userservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import spiring.exapmle.userservice.models.User;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByRole_Name(String roleName);
    List<User> findByNameContainingIgnoreCase(String search);
}