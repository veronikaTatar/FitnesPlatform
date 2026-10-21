package spiring.exapmle.authservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import spiring.exapmle.authservice.model.AuthUser;

import java.util.Optional;

public interface AuthUserRepository extends JpaRepository<AuthUser, Long> {
    Optional<AuthUser> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
}
