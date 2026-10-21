package spiring.exapmle.userservice.services;

import spiring.exapmle.userservice.models.Client;
import spiring.exapmle.userservice.models.Notification;
import spiring.exapmle.userservice.models.Trainer;
import spiring.exapmle.userservice.models.User;
import java.util.List;

public interface UserService {
    User getById(Long id);
    User getByEmail(String email);
    List<User> getAll();
    List<User> getByRole(String roleName);
    List<User> search(String query);

    Client getClientProfile(Long idClient);
    Client getClientByUser(Long idUser);
    Trainer getTrainerProfile(Long idTrainer);
    List<Trainer> getTrainersBySpecialization(String specialization);

    List<Notification> getNotifications(Long idUser);
    List<Notification> getUnreadNotifications(Long idUser);
}