package spiring.exapmle.apigateway.graphql.mutation;

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

    @MutationMapping
    public AuthPayloadDto register(@Argument RegisterInputDto input) {
        return authServiceClient.register(input);
    }

    @MutationMapping
    public AuthPayloadDto login(@Argument LoginInputDto input) {
        return authServiceClient.login(input);
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
