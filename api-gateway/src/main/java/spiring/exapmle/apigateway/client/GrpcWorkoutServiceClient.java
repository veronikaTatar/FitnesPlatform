package spiring.exapmle.apigateway.client;

import com.fitness.workout.grpc.*;
import com.fitness.user.grpc.*;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import spiring.exapmle.apigateway.dto.*;
import java.util.List;

@Service @Primary
public class GrpcWorkoutServiceClient implements WorkoutServiceClient {
 private final WorkoutServiceGrpc.WorkoutServiceBlockingStub grpc; private final UserServiceGrpc.UserServiceBlockingStub users; private final WorkoutServiceClient fallback;
 public GrpcWorkoutServiceClient(WorkoutServiceGrpc.WorkoutServiceBlockingStub grpc,UserServiceGrpc.UserServiceBlockingStub users,@org.springframework.beans.factory.annotation.Qualifier("mockWorkoutServiceClient") WorkoutServiceClient fallback){this.grpc=grpc;this.users=users;this.fallback=fallback;}
 public List<TrainingProgramDto> getPrograms(String type,String level,Long trainer){return getPrograms(type,level,trainer,null,null);}
 public List<TrainingProgramDto> getPrograms(String type,String level,Long trainer,String sort,String order){return grpc.getTrainingPrograms(ProgramsRequest.newBuilder().setType(v(type)).setDifficultyLevel(v(level)).setTrainerId(n(trainer)).setSortBy(v(sort)).setSortOrder(v(order)).build()).getProgramsList().stream().map(this::program).toList();}
 public TrainingProgramDto getProgram(Long id){return program(grpc.getTrainingProgram(GetProgramRequest.newBuilder().setIdProgram(id).build()));}
 public List<WorkoutDto> getWorkouts(Long t,Long p,String from,String to,String status){return getWorkouts(t,p,from,to,status,null,null);}
 public List<WorkoutDto> getWorkouts(Long t,Long p,String from,String to,String status,String sort,String order){return grpc.getWorkouts(WorkoutsRequest.newBuilder().setTrainerId(n(t)).setProgramId(n(p)).setDateFrom(v(from)).setDateTo(v(to)).setStatus(v(status)).setSortBy(v(sort)).setSortOrder(v(order)).build()).getWorkoutsList().stream().map(this::workout).toList();}
 public WorkoutDto getWorkout(Long id){return workout(grpc.getWorkout(GetWorkoutRequest.newBuilder().setIdWorkout(id).build()));}
 public List<ProgressDto> getProgressByClient(Long id){return getProgressByClientAndPeriod(id,null,null);}
 public List<ProgressDto> getProgressByClientAndPeriod(Long id,String from,String to){return grpc.getProgressByClient(ProgressRequest.newBuilder().setClientId(id).setDateFrom(v(from)).setDateTo(v(to)).build()).getItemsList().stream().map(x->{ProgressDto d=new ProgressDto();d.setIdProgress(x.getIdProgress());d.setDate(x.getDate());d.setValue((float)x.getValue());d.setTrainerComment(x.getTrainerComment());try{d.setClient(GrpcUserServiceClient.client(users.getClient(GetClientRequest.newBuilder().setIdClient(x.getIdClient()).build())));}catch(RuntimeException ignored){}return d;}).toList();}
 private TrainingProgramDto program(ProgramResponse x){TrainingProgramDto d=new TrainingProgramDto();d.setIdTrainingProgram(x.getIdTrainingProgram());d.setName(x.getName());d.setDescription(x.getDescription());d.setType(x.getType());d.setDifficultyLevel(x.getDifficultyLevel());d.setDuration(x.getDuration());try{d.setTrainer(GrpcUserServiceClient.trainer(users.getTrainer(GetTrainerRequest.newBuilder().setIdTrainer(x.getIdTrainer()).build())));}catch(RuntimeException ignored){}return d;}
 private WorkoutDto workout(WorkoutResponse x){WorkoutDto d=new WorkoutDto();d.setIdWorkout(x.getIdWorkout());d.setStartDatetime(x.getStartDatetime());d.setDuration(x.getDuration());d.setStatus(x.getStatus().name());d.setMaxCapacity(x.getMaxCapacity());d.setTrainingProgram(program(grpc.getTrainingProgram(GetProgramRequest.newBuilder().setIdProgram(x.getIdTrainingProgram()).build())));try{d.setTrainer(GrpcUserServiceClient.trainer(users.getTrainer(GetTrainerRequest.newBuilder().setIdTrainer(x.getIdTrainer()).build())));}catch(RuntimeException ignored){}return d;}
 private static long n(Long x){return x==null?0:x;} private static String v(String x){return x==null?"":x;}
 public TrainingProgramDto createTrainingProgram(CreateTrainingProgramInputDto x){return fallback.createTrainingProgram(x);}public TrainingProgramDto updateTrainingProgram(Long x,UpdateTrainingProgramInputDto y){return fallback.updateTrainingProgram(x,y);}public Boolean archiveTrainingProgram(Long x){return fallback.archiveTrainingProgram(x);}public WorkoutDto createWorkout(CreateWorkoutInputDto x){return fallback.createWorkout(x);}public WorkoutDto updateWorkout(Long x,UpdateWorkoutInputDto y){return fallback.updateWorkout(x,y);}public WorkoutDto changeWorkoutStatus(Long x,String y){return fallback.changeWorkoutStatus(x,y);}public ProgressDto logProgress(LogProgressInputDto x){return fallback.logProgress(x);}
}
