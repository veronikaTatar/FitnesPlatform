package spiring.exapmle.userservice.controllers;

import com.fitness.user.grpc.*;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.transaction.annotation.Transactional;
import spiring.exapmle.userservice.models.Client;
import spiring.exapmle.userservice.models.Trainer;
import spiring.exapmle.userservice.models.User;
import spiring.exapmle.userservice.services.UserService;

import java.util.Locale;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@GrpcService
public class UserGrpcController extends UserServiceGrpc.UserServiceImplBase {
    private final UserService userService;

    public UserGrpcController(UserService userService) {
        this.userService = userService;
    }

    @Override
    @Transactional(readOnly = true)
    public void getUser(GetUserRequest request, StreamObserver<UserResponse> observer) {
        respond(observer, () -> toUser(userService.getById(request.getIdUser())));
    }

    @Override
    @Transactional(readOnly = true)
    public void getClient(GetClientRequest request, StreamObserver<ClientResponse> observer) {
        respond(observer, () -> toClient(userService.getClientProfile(request.getIdClient())));
    }

    @Override
    @Transactional(readOnly = true)
    public void getClientByUser(GetClientByUserRequest request,
                                StreamObserver<ClientResponse> observer) {
        respond(observer, () -> toClient(userService.getClientByUser(request.getIdUser())));
    }

    @Override
    @Transactional(readOnly = true)
    public void getClientByEmail(GetClientByEmailRequest request, StreamObserver<ClientResponse> observer) {
        respond(observer, () -> toClient(userService.getClientByEmail(request.getEmail())));
    }

    @Override
    public void ensureUserProfile(EnsureUserProfileRequest request, StreamObserver<UserResponse> observer) {
        respond(observer, () -> toUser(userService.ensureUserProfile(
                request.getName(), request.getEmail(), request.getPhone(), request.getRole(),
                request.getBirthDate(), request.getFitnessLevel(), request.getSpecialization())));
    }

    @Override
    @Transactional(readOnly = true)
    public void getTrainer(GetTrainerRequest request, StreamObserver<TrainerResponse> observer) {
        respond(observer, () -> toTrainer(userService.getTrainerProfile(request.getIdTrainer())));
    }

    @Override
    @Transactional(readOnly = true)
    public void getTrainerByUser(GetTrainerByUserRequest request,
                                 StreamObserver<TrainerResponse> observer) {
        respond(observer, () -> toTrainer(userService.getTrainerByUser(request.getIdUser())));
    }

    @Override
    @Transactional(readOnly = true)
    public void checkUserExists(CheckUserExistsRequest request,
                               StreamObserver<CheckUserExistsResponse> observer) {
        respond(observer, () -> CheckUserExistsResponse.newBuilder()
                .setExists(userService.existsByEmail(request.getEmail()))
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public void getUsers(GetUsersRequest request, StreamObserver<GetUsersResponse> observer) {
        respond(observer, () -> {
            String role = request.getRole().isBlank() ? null : request.getRole();
            String search = request.getSearch().isBlank() ? null : request.getSearch();
            var users = userService.getAll().stream()
                    .filter(u -> role == null || role.equalsIgnoreCase(u.getRole().getName()))
                    .filter(u -> search == null || (u.getName() != null
                            && u.getName().toLowerCase(Locale.ROOT)
                            .contains(search.toLowerCase(Locale.ROOT))))
                    .map(UserGrpcController::toUser)
                    .collect(Collectors.toList());
            return GetUsersResponse.newBuilder().addAllUsers(users).build();
        });
    }

    @Override
    @Transactional(readOnly = true)
    public void getUsersByIds(GetUsersByIdsRequest request,
                              StreamObserver<GetUsersByIdsResponse> observer) {
        respond(observer, () -> GetUsersByIdsResponse.newBuilder()
                .addAllUsers(request.getIdsList().stream()
                        .map(userService::getById)
                        .map(UserGrpcController::toUser)
                        .collect(Collectors.toList()))
                .build());
    }

    private static UserResponse toUser(User user) {
        return UserResponse.newBuilder()
                .setIdUser(user.getIdUser())
                .setName(value(user.getName()))
                .setEmail(value(user.getEmail()))
                .setPhone(value(user.getPhone()))
                .setRole(toRole(user.getRole().getName()))
                .build();
    }

    private static ClientResponse toClient(Client client) {
        return ClientResponse.newBuilder()
                .setIdClient(client.getIdClient())
                .setIdUser(client.getUser().getIdUser())
                .setName(value(client.getUser().getName()))
                .setBirthDate(client.getBirthDate() == null ? "" : client.getBirthDate().toString())
                .setFitnessLevel(value(client.getFitnessLevel()))
                .build();
    }

    private static TrainerResponse toTrainer(Trainer trainer) {
        return TrainerResponse.newBuilder()
                .setIdTrainer(trainer.getIdTrainer())
                .setIdUser(trainer.getUser().getIdUser())
                .setName(value(trainer.getUser().getName()))
                .setSpecialization(value(trainer.getSpecialization()))
                .setRating(trainer.getRating() == null ? 0.0 : trainer.getRating().doubleValue())
                .build();
    }

    private static UserRole toRole(String role) {
        return switch (role.toLowerCase(Locale.ROOT)) {
            case "client" -> UserRole.CLIENT;
            case "trainer" -> UserRole.TRAINER;
            case "admin" -> UserRole.ADMIN;
            default -> UserRole.USER_ROLE_UNKNOWN;
        };
    }

    private static String value(String value) {
        return value == null ? "" : value;
    }

    private static <T> void respond(StreamObserver<T> observer, Supplier<T> action) {
        try {
            observer.onNext(action.get());
            observer.onCompleted();
        } catch (StatusRuntimeException exception) {
            observer.onError(exception);
        } catch (IllegalArgumentException exception) {
            observer.onError(Status.INVALID_ARGUMENT
                    .withDescription(exception.getMessage()).asRuntimeException());
        } catch (RuntimeException exception) {
            Status status = exception.getMessage() != null
                    && exception.getMessage().toLowerCase(Locale.ROOT).contains("not found")
                    ? Status.NOT_FOUND : Status.INTERNAL;
            observer.onError(status.withDescription(exception.getMessage()).asRuntimeException());
        }
    }
}
