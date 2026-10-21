package spiring.exapmle.userservice.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import spiring.exapmle.userservice.models.*;
import spiring.exapmle.userservice.repositories.*;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final TrainerRepository trainerRepository;
    private final NotificationRepository notificationRepository;

    @Override
    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
    }

    @Override
    public User getByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found by email: " + email));
    }

    @Override
    public List<User> getAll() {
        return userRepository.findAll();
    }

    @Override
    public List<User> getByRole(String roleName) {
        return userRepository.findByRole_Name(roleName);
    }

    @Override
    public List<User> search(String query) {
        return userRepository.findByNameContainingIgnoreCase(query);
    }

    @Override
    public Client getClientProfile(Long idClient) {
        return clientRepository.findById(idClient)
                .orElseThrow(() -> new RuntimeException("Client not found: " + idClient));
    }

    @Override
    public Client getClientByUser(Long idUser) {
        return clientRepository.findByUser_IdUser(idUser)
                .orElseThrow(() -> new RuntimeException("Client profile for user " + idUser + " not found"));
    }

    @Override
    public Trainer getTrainerProfile(Long idTrainer) {
        return trainerRepository.findById(idTrainer)
                .orElseThrow(() -> new RuntimeException("Trainer not found: " + idTrainer));
    }

    @Override
    public List<Trainer> getTrainersBySpecialization(String specialization) {
        return trainerRepository.findBySpecializationContainingIgnoreCase(specialization);
    }

    @Override
    public List<Notification> getNotifications(Long idUser) {
        return notificationRepository.findByUser_IdUserOrderByDateDesc(idUser);
    }

    @Override
    public List<Notification> getUnreadNotifications(Long idUser) {
        return notificationRepository.findByUser_IdUserAndIsRead(idUser, false);
    }
}