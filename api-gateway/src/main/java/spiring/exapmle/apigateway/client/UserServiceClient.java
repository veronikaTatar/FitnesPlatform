package spiring.exapmle.apigateway.client;

import spiring.exapmle.apigateway.dto.*;

import java.util.List;

public interface UserServiceClient {
    UserDto getUser(Long id);
    List<UserDto> getUsers(String role, String search);
    UserDto getCurrentUser();
    List<NotificationDto> getNotifications(Boolean isRead);
    AuthPayloadDto register(RegisterInputDto input);
    AuthPayloadDto login(LoginInputDto input);
    Boolean logout();
    UserDto updateProfile(UpdateProfileInputDto input);
    UserDto updateUserRole(Long idUser, String roleName);
    UserDto blockUser(Long idUser, Boolean blocked);
    NotificationDto markNotificationRead(Long idNotification);
    Boolean markAllNotificationsRead();
}