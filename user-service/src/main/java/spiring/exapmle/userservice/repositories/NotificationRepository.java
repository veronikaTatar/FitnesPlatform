package spiring.exapmle.userservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import spiring.exapmle.userservice.models.Notification;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUser_IdUserOrderByDateDesc(Long idUser);
    List<Notification> findByUser_IdUserAndIsRead(Long idUser, Boolean isRead);
}