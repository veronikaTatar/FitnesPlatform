package spiring.exapmle.userservice.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import spiring.exapmle.userservice.models.*;
import spiring.exapmle.userservice.repositories.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final TrainerRepository trainerRepository;
    private final NotificationRepository notificationRepository;
    private final RoleRepository roleRepository;

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
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
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
    public Client getClientByEmail(String email) {
        return clientRepository.findByUser_EmailIgnoreCase(email.trim())
                .orElseThrow(() -> new RuntimeException("Client profile for email " + email + " not found"));
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public User ensureUserProfile(String name, String email, String phone, String roleName,
                                  String birthDate, String fitnessLevel, String specialization) {
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        String normalizedRole = roleName.trim().toLowerCase(Locale.ROOT);
        var existing = userRepository.findByEmail(normalizedEmail);
        if (existing.isPresent()) {
            User user = existing.get();
            if (!normalizedRole.equalsIgnoreCase(user.getRole().getName())) {
                throw new IllegalArgumentException("Registered role does not match User Service profile");
            }
            return user;
        }

        if (!normalizedRole.equals("client") && !normalizedRole.equals("trainer")
                && !normalizedRole.equals("admin")) {
            throw new IllegalArgumentException("Only client, trainer, and admin profiles are supported");
        }
        Role role = roleRepository.findByName(normalizedRole)
                .orElseThrow(() -> new IllegalArgumentException("Unknown role: " + normalizedRole));
        User user = new User();
        user.setName(name.trim());
        user.setEmail(normalizedEmail);
        user.setPhone(phone == null || phone.isBlank() ? null : phone.trim());
        user.setPassword("managed-by-auth-service");
        user.setRole(role);
        user = userRepository.save(user);

        if (normalizedRole.equals("client")) {
            Client client = new Client();
            client.setUser(user);
            client.setBirthDate(birthDate == null || birthDate.isBlank() ? null : LocalDate.parse(birthDate));
            client.setFitnessLevel(fitnessLevel == null || fitnessLevel.isBlank() ? null : fitnessLevel.trim());
            clientRepository.save(client);
        } else if (normalizedRole.equals("trainer")) {
            Trainer trainer = new Trainer();
            trainer.setUser(user);
            trainer.setSpecialization(specialization == null || specialization.isBlank() ? null : specialization.trim());
            trainer.setRating(java.math.BigDecimal.ZERO);
            trainerRepository.save(trainer);
        }
        return user;
    }

    @Override
    public Trainer getTrainerProfile(Long idTrainer) {
        return trainerRepository.findById(idTrainer)
                .orElseThrow(() -> new RuntimeException("Trainer not found: " + idTrainer));
    }

    @Override
    public Trainer getTrainerByUser(Long idUser) {
        return trainerRepository.findByUser_IdUser(idUser)
                .orElseThrow(() -> new RuntimeException(
                        "Trainer profile for user " + idUser + " not found"));
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
