package spiring.exapmle.userservice.services;

import spiring.exapmle.userservice.models.Client;
import spiring.exapmle.userservice.models.Notification;
import spiring.exapmle.userservice.models.Trainer;
import spiring.exapmle.userservice.models.User;
import java.util.List;

public interface UserService {
    User getById(Long id);
    User getByEmail(String email);
    boolean existsByEmail(String email);
    List<User> getAll();
    List<User> getByRole(String roleName);
    List<User> search(String query);

    Client getClientProfile(Long idClient);
    Client getClientByUser(Long idUser);
    Client getClientByEmail(String email);
    User ensureUserProfile(String name, String email, String phone, String role,
                           String birthDate, String fitnessLevel, String specialization);
    Trainer getTrainerProfile(Long idTrainer);
    Trainer getTrainerByUser(Long idUser);
    List<Trainer> getTrainersBySpecialization(String specialization);

    List<Notification> getNotifications(Long idUser);
    List<Notification> getUnreadNotifications(Long idUser);
}
