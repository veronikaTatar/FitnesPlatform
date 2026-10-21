package spiring.exapmle.authservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import spiring.exapmle.authservice.model.Role;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByNameIgnoreCase(String name);
}
