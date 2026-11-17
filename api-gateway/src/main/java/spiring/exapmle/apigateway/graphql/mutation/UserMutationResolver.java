package spiring.exapmle.apigateway.graphql.mutation;

import com.fitness.user.grpc.EnsureUserProfileRequest;
import com.fitness.user.grpc.UserServiceGrpc;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;
import spiring.exapmle.apigateway.client.AuthServiceClient;
import spiring.exapmle.apigateway.client.UserServiceClient;
import spiring.exapmle.apigateway.dto.*;

@Controller
@RequiredArgsConstructor
public class UserMutationResolver {

    private final UserServiceClient userServiceClient;
    private final AuthServiceClient authServiceClient;
    private final UserServiceGrpc.UserServiceBlockingStub userService;

    @MutationMapping
    public AuthPayloadDto register(@Argument RegisterInputDto input) {
        AuthPayloadDto payload = authServiceClient.register(input);
        ensureUserProfile(payload, input.getBirthDate(), input.getFitnessLevel(),
                input.getSpecialization());
        return payload;
    }

    @MutationMapping
    public AuthPayloadDto login(@Argument LoginInputDto input) {
        AuthPayloadDto payload = authServiceClient.login(input);
        ensureUserProfile(payload, null, null, null);
        return payload;
    }

    private void ensureUserProfile(AuthPayloadDto payload, String birthDate,
                                   String fitnessLevel, String specialization) {
        UserDto user = payload.getUser();
        userService.ensureUserProfile(EnsureUserProfileRequest.newBuilder()
                .setName(user.getName() == null ? "" : user.getName())
                .setEmail(user.getEmail() == null ? "" : user.getEmail())
                .setPhone(user.getPhone() == null ? "" : user.getPhone())
                .setRole(user.getRole() == null || user.getRole().getName() == null
                        ? "" : user.getRole().getName())
                .setBirthDate(birthDate == null ? "" : birthDate)
                .setFitnessLevel(fitnessLevel == null ? "" : fitnessLevel)
                .setSpecialization(specialization == null ? "" : specialization)
                .build());
    }

    @MutationMapping
    public Boolean logout() {
        return userServiceClient.logout();
    }

    @MutationMapping
    public UserDto updateProfile(@Argument UpdateProfileInputDto input) {
        return userServiceClient.updateProfile(input);
    }

    @MutationMapping
    public UserDto updateUserRole(@Argument Long idUser,
                                  @Argument String roleName) {
        return userServiceClient.updateUserRole(idUser, roleName);
    }

    @MutationMapping
    public UserDto blockUser(@Argument Long idUser,
                             @Argument Boolean blocked) {
        return userServiceClient.blockUser(idUser, blocked);
    }

    @MutationMapping
    public NotificationDto markNotificationRead(@Argument Long idNotification) {
        return userServiceClient.markNotificationRead(idNotification);
    }

    @MutationMapping
    public Boolean markAllNotificationsRead() {
        return userServiceClient.markAllNotificationsRead();
    }
}
