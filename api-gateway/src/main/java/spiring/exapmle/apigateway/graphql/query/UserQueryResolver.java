package spiring.exapmle.apigateway.graphql.query;

import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import spiring.exapmle.apigateway.client.UserServiceClient;
import spiring.exapmle.apigateway.dto.UserDto;
import spiring.exapmle.apigateway.dto.NotificationDto;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class UserQueryResolver {

    private final UserServiceClient userServiceClient;

    @QueryMapping
    public UserDto me() {
        return userServiceClient.getCurrentUser();
    }

    @QueryMapping
    public UserDto user(@Argument Long id) {
        return userServiceClient.getUser(id);
    }

    @QueryMapping
    public List<UserDto> users(@Argument String role,
                               @Argument String search) {
        return userServiceClient.getUsers(role, search);
    }

    @QueryMapping
    public List<NotificationDto> myNotifications(@Argument Boolean isRead) {
        return userServiceClient.getNotifications(isRead);
    }
}