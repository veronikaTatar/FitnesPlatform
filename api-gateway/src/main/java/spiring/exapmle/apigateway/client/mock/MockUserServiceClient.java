package spiring.exapmle.apigateway.client.mock;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import spiring.exapmle.apigateway.client.UserServiceClient;
import spiring.exapmle.apigateway.dto.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Primary
public class MockUserServiceClient implements UserServiceClient {

    private final Map<Long, UserDto> storage = new HashMap<>();
    private final List<NotificationDto> notifications = new ArrayList<>();

    public MockUserServiceClient() {
        RoleDto clientRole  = role(1L, "client");
        RoleDto trainerRole = role(2L, "trainer");

        UserDto u1 = user(1L, "Иванов Иван",    "ivanov@fit.by", "+375291112233", clientRole);
        u1.setClient(client(1L, "1995-03-15", "beginner"));

        UserDto u2 = user(2L, "Кузнецова Анна", "anna@fit.by",   "+375292223344", clientRole);
        u2.setClient(client(2L, "1992-07-22", "intermediate"));

        UserDto u3 = user(3L, "Петров Пётр",    "petrov@fit.by", "+375293334455", trainerRole);
        u3.setTrainer(trainer(1L, "Йога", 4.8f));

        storage.put(1L, u1);
        storage.put(2L, u2);
        storage.put(3L, u3);

        // Уведомления для клиента с idUser = 1
        NotificationDto n1 = new NotificationDto();
        n1.setIdNotification(1L);
        n1.setText("Добро пожаловать в фитнес-платформу!");
        n1.setDate("2026-10-01T09:00:00");
        n1.setIsRead(false);
        n1.setType("SYSTEM");

        NotificationDto n2 = new NotificationDto();
        n2.setIdNotification(2L);
        n2.setText("Напоминание: занятие завтра в 18:00");
        n2.setDate("2026-10-04T18:00:00");
        n2.setIsRead(false);
        n2.setType("WORKOUT");

        NotificationDto n3 = new NotificationDto();
        n3.setIdNotification(3L);
        n3.setText("Ваш абонемент активирован");
        n3.setDate("2026-09-15T12:30:00");
        n3.setIsRead(true);
        n3.setType("SUBSCRIPTION");

        notifications.add(n1);
        notifications.add(n2);
        notifications.add(n3);
    }

    @Override
    public UserDto getUser(Long id) {
        UserDto u = storage.get(id);
        if (u == null) throw new RuntimeException("User not found: " + id);
        return u;
    }

    @Override
    public List<UserDto> getUsers(String role, String search) {
        List<UserDto> result = new ArrayList<>(storage.values());
        if (role != null) {
            result = result.stream()
                    .filter(u -> u.getRole() != null && role.equals(u.getRole().getName()))
                    .collect(Collectors.toList());
        }
        if (search != null && !search.isBlank()) {
            String s = search.toLowerCase();
            result = result.stream()
                    .filter(u -> u.getName() != null && u.getName().toLowerCase().contains(s))
                    .collect(Collectors.toList());
        }
        return result;
    }

    @Override
    public UserDto getCurrentUser() {
        return storage.get(1L);
    }

    @Override
    public List<NotificationDto> getNotifications(Boolean isRead) {
        return notifications.stream()
                .filter(n -> isRead == null || isRead.equals(n.getIsRead()))
                .collect(Collectors.toList());
    }

    // ================== Вспомогательные фабрики для заполнения конструктора ==================

    private static RoleDto role(Long id, String name) {
        RoleDto r = new RoleDto();
        r.setIdRole(id);
        r.setName(name);
        return r;
    }

    private static UserDto user(Long id, String name, String email, String phone, RoleDto role) {
        UserDto u = new UserDto();
        u.setIdUser(id);
        u.setName(name);
        u.setEmail(email);
        u.setPhone(phone);
        u.setRole(role);
        return u;
    }

    private static ClientDto client(Long id, String birthDate, String fitnessLevel) {
        ClientDto c = new ClientDto();
        c.setIdClient(id);
        c.setBirthDate(birthDate);
        c.setFitnessLevel(fitnessLevel);
        return c;
    }

    private static TrainerDto trainer(Long id, String spec, Float rating) {
        TrainerDto t = new TrainerDto();
        t.setIdTrainer(id);
        t.setSpecialization(spec);
        t.setRating(rating);
        return t;
    }


    // ================== Бизнес-операции (ЛР4) ==================

    @Override
    public AuthPayloadDto register(RegisterInputDto input) {
        boolean exists = storage.values().stream()
                .anyMatch(u -> input.getEmail().equals(u.getEmail()));
        if (exists) {
            throw new RuntimeException("User with email already exists: "
                    + input.getEmail());
        }

        UserDto user = new UserDto();
        user.setIdUser((long) (storage.size() + 100));
        user.setName(input.getName());
        user.setEmail(input.getEmail());
        user.setPhone(input.getPhone());
        user.setRole(role("trainer".equals(input.getRole()) ? 2L : 1L,
                input.getRole()));

        if ("client".equals(input.getRole())) {
            user.setClient(client(user.getIdUser(),
                    input.getBirthDate(), input.getFitnessLevel()));
        } else if ("trainer".equals(input.getRole())) {
            user.setTrainer(trainer(user.getIdUser(),
                    input.getSpecialization(), 0.0f));
        }

        storage.put(user.getIdUser(), user);

        AuthPayloadDto payload = new AuthPayloadDto();
        payload.setToken("mock-jwt-token-" + user.getIdUser());
        payload.setUser(user);
        return payload;
    }

    @Override
    public AuthPayloadDto login(LoginInputDto input) {
        UserDto user = storage.values().stream()
                .filter(u -> input.getEmail().equals(u.getEmail()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("User not found: "
                        + input.getEmail()));

        AuthPayloadDto payload = new AuthPayloadDto();
        payload.setToken("mock-jwt-token-" + user.getIdUser());
        payload.setUser(user);
        return payload;
    }

    @Override
    public Boolean logout() {
        return true;
    }

    @Override
    public UserDto updateProfile(UpdateProfileInputDto input) {
        UserDto u = storage.get(1L);
        if (input.getName() != null) u.setName(input.getName());
        if (input.getPhone() != null) u.setPhone(input.getPhone());
        if (u.getClient() != null) {
            if (input.getBirthDate() != null)
                u.getClient().setBirthDate(input.getBirthDate());
            if (input.getFitnessLevel() != null)
                u.getClient().setFitnessLevel(input.getFitnessLevel());
        }
        if (u.getTrainer() != null && input.getSpecialization() != null) {
            u.getTrainer().setSpecialization(input.getSpecialization());
        }
        return u;
    }

    @Override
    public UserDto updateUserRole(Long idUser, String roleName) {
        UserDto u = getUser(idUser);
        u.setRole(role("trainer".equals(roleName) ? 2L : 1L, roleName));
        return u;
    }

    @Override
    public UserDto blockUser(Long idUser, Boolean blocked) {
        UserDto u = getUser(idUser);
        // в mock-реализации поле можно хранить строкой в phone или отдельным полем
        return u;
    }

    @Override
    public NotificationDto markNotificationRead(Long idNotification) {
        NotificationDto n = notifications.stream()
                .filter(x -> idNotification.equals(x.getIdNotification()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(
                        "Notification not found: " + idNotification));
        n.setIsRead(true);
        return n;
    }

    @Override
    public Boolean markAllNotificationsRead() {
        notifications.forEach(n -> n.setIsRead(true));
        return true;
    }
}