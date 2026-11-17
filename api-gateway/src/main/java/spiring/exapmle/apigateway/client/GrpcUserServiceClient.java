package spiring.exapmle.apigateway.client;

import com.fitness.user.grpc.*;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import spiring.exapmle.apigateway.dto.*;
import java.util.List;

@Service @Primary
public class GrpcUserServiceClient implements UserServiceClient {
    private final UserServiceGrpc.UserServiceBlockingStub grpc;
    private final UserServiceClient fallback;
    public GrpcUserServiceClient(UserServiceGrpc.UserServiceBlockingStub grpc,
                                 @org.springframework.beans.factory.annotation.Qualifier("mockUserServiceClient") UserServiceClient fallback) {
        this.grpc=grpc; this.fallback=fallback;
    }
    public UserDto getUser(Long id) { return user(grpc.getUser(GetUserRequest.newBuilder().setIdUser(id).build())); }
    public List<UserDto> getUsers(String role,String search) { return grpc.getUsers(GetUsersRequest.newBuilder().setRole(v(role)).setSearch(v(search)).build()).getUsersList().stream().map(this::user).toList(); }
    private UserDto user(com.fitness.user.grpc.UserResponse x) {
        UserDto d=new UserDto(); d.setIdUser(x.getIdUser()); d.setName(x.getName()); d.setEmail(x.getEmail()); d.setPhone(x.getPhone());
        RoleDto r=new RoleDto(); r.setIdRole((long)x.getRoleValue()); r.setName(x.getRole().name().toLowerCase()); d.setRole(r);
        try { if(x.getRole()==UserRole.CLIENT) d.setClient(client(grpc.getClientByUser(GetClientByUserRequest.newBuilder().setIdUser(x.getIdUser()).build())));
              else if(x.getRole()==UserRole.TRAINER) d.setTrainer(trainer(grpc.getTrainerByUser(GetTrainerByUserRequest.newBuilder().setIdUser(x.getIdUser()).build()))); } catch(RuntimeException ignored) { }
        return d;
    }
    static ClientDto client(ClientResponse x) { ClientDto d=new ClientDto(); d.setIdClient(x.getIdClient()); d.setBirthDate(x.getBirthDate()); d.setFitnessLevel(x.getFitnessLevel()); return d; }
    static TrainerDto trainer(TrainerResponse x) { TrainerDto d=new TrainerDto(); d.setIdTrainer(x.getIdTrainer()); d.setSpecialization(x.getSpecialization()); d.setRating((float)x.getRating()); return d; }
    private static String v(String x){return x==null?"":x;}
    public UserDto getCurrentUser(){return fallback.getCurrentUser();} public List<NotificationDto> getNotifications(Boolean x){return fallback.getNotifications(x);}
    public AuthPayloadDto register(RegisterInputDto x){return fallback.register(x);} public AuthPayloadDto login(LoginInputDto x){return fallback.login(x);} public Boolean logout(){return fallback.logout();}
    public UserDto updateProfile(UpdateProfileInputDto x){return fallback.updateProfile(x);} public UserDto updateUserRole(Long x,String y){return fallback.updateUserRole(x,y);} public UserDto blockUser(Long x,Boolean y){return fallback.blockUser(x,y);}
    public NotificationDto markNotificationRead(Long x){return fallback.markNotificationRead(x);} public Boolean markAllNotificationsRead(){return fallback.markAllNotificationsRead();}
}
