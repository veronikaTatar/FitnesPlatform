package spiring.exapmle.apigateway.client;

import com.fitness.workout.grpc.*;
import com.fitness.user.grpc.*;
import org.springframework.context.annotation.Primary;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import spiring.exapmle.apigateway.dto.*;
import java.util.List;

@Service @Primary
public class GrpcWorkoutServiceClient implements WorkoutServiceClient {
 private final WorkoutServiceGrpc.WorkoutServiceBlockingStub grpc; private final UserServiceGrpc.UserServiceBlockingStub users;
 public GrpcWorkoutServiceClient(WorkoutServiceGrpc.WorkoutServiceBlockingStub grpc,UserServiceGrpc.UserServiceBlockingStub users){this.grpc=grpc;this.users=users;}
 @Cacheable(cacheNames="programs", key="#root.methodName + ':' + T(java.util.Arrays).toString(#root.args)")
 public List<TrainingProgramDto> getPrograms(String type,String level,Long trainer){return getPrograms(type,level,trainer,null,null);}
 @Cacheable(cacheNames="programs", key="#root.methodName + ':' + T(java.util.Arrays).toString(#root.args)")
 public List<TrainingProgramDto> getPrograms(String type,String level,Long trainer,String sort,String order){return grpc.getTrainingPrograms(ProgramsRequest.newBuilder().setType(v(type)).setDifficultyLevel(v(level)).setTrainerId(n(trainer)).setSortBy(v(sort)).setSortOrder(v(order)).build()).getProgramsList().stream().map(this::program).toList();}
 @Cacheable(cacheNames="program", key="#p0")
 public TrainingProgramDto getProgram(Long id){return program(grpc.getTrainingProgram(GetProgramRequest.newBuilder().setIdProgram(id).build()));}
 @Cacheable(cacheNames="workouts", key="#root.methodName + ':' + T(java.util.Arrays).toString(#root.args)")
 public List<WorkoutDto> getWorkouts(Long t,Long p,String from,String to,String status){return getWorkouts(t,p,from,to,status,null,null);}
 @Cacheable(cacheNames="workouts", key="#root.methodName + ':' + T(java.util.Arrays).toString(#root.args)")
 public List<WorkoutDto> getWorkouts(Long t,Long p,String from,String to,String status,String sort,String order){return grpc.getWorkouts(WorkoutsRequest.newBuilder().setTrainerId(n(t)).setProgramId(n(p)).setDateFrom(v(from)).setDateTo(v(to)).setStatus(v(status)).setSortBy(v(sort)).setSortOrder(v(order)).build()).getWorkoutsList().stream().map(this::workout).toList();}
 @Cacheable(cacheNames="workout", key="#p0")
 public WorkoutDto getWorkout(Long id){return workout(grpc.getWorkout(GetWorkoutRequest.newBuilder().setIdWorkout(id).build()));}
 @Cacheable(cacheNames="progress", key="#p0 + ':'")
 public List<ProgressDto> getProgressByClient(Long id){return getProgressByClientAndPeriod(id,null,null);}
 @Cacheable(cacheNames="progress", key="#p0 + ':' + T(java.util.Objects).toString(#p1,'') + ':' + T(java.util.Objects).toString(#p2,'')")
 public List<ProgressDto> getProgressByClientAndPeriod(Long id,String from,String to){return grpc.getProgressByClient(ProgressRequest.newBuilder().setClientId(id).setDateFrom(v(from)).setDateTo(v(to)).build()).getItemsList().stream().map(x->{ProgressDto d=new ProgressDto();d.setIdProgress(x.getIdProgress());d.setDate(x.getDate());d.setValue((float)x.getValue());d.setTrainerComment(x.getTrainerComment());try{d.setClient(GrpcUserServiceClient.client(users.getClient(GetClientRequest.newBuilder().setIdClient(x.getIdClient()).build())));}catch(RuntimeException ignored){}return d;}).toList();}
 private TrainingProgramDto program(ProgramResponse x){TrainingProgramDto d=new TrainingProgramDto();d.setIdTrainingProgram(x.getIdTrainingProgram());d.setName(x.getName());d.setDescription(x.getDescription());d.setType(x.getType());d.setDifficultyLevel(x.getDifficultyLevel());d.setDuration(x.getDuration());try{d.setTrainer(GrpcUserServiceClient.trainer(users.getTrainer(GetTrainerRequest.newBuilder().setIdTrainer(x.getIdTrainer()).build())));}catch(RuntimeException ignored){}return d;}
 private WorkoutDto workout(WorkoutResponse x){WorkoutDto d=new WorkoutDto();d.setIdWorkout(x.getIdWorkout());d.setStartDatetime(x.getStartDatetime());d.setDuration(x.getDuration());d.setStatus(x.getStatus().name());d.setMaxCapacity(x.getMaxCapacity());d.setTrainingProgram(program(grpc.getTrainingProgram(GetProgramRequest.newBuilder().setIdProgram(x.getIdTrainingProgram()).build())));try{d.setTrainer(GrpcUserServiceClient.trainer(users.getTrainer(GetTrainerRequest.newBuilder().setIdTrainer(x.getIdTrainer()).build())));}catch(RuntimeException ignored){}return d;}
 private static long n(Long x){return x==null?0:x;} private static String v(String x){return x==null?"":x;}
 @CacheEvict(cacheNames={"programs","program","workouts"}, allEntries=true)
 public TrainingProgramDto createTrainingProgram(Long userId,CreateTrainingProgramInputDto x){
  long trainerId=users.getTrainerByUser(GetTrainerByUserRequest.newBuilder().setIdUser(userId).build()).getIdTrainer();
  return program(grpc.createTrainingProgram(CreateTrainingProgramRequest.newBuilder().setTrainerId(trainerId).setName(v(x.getName())).setDescription(v(x.getDescription())).setType(v(x.getType())).setDifficultyLevel(v(x.getDifficultyLevel())).setDuration(x.getDuration()==null?0:x.getDuration()).build()));
 }
 @CacheEvict(cacheNames={"programs","program","workouts"}, allEntries=true)
 public TrainingProgramDto updateTrainingProgram(Long id,UpdateTrainingProgramInputDto x){return program(grpc.updateTrainingProgram(UpdateTrainingProgramRequest.newBuilder().setIdProgram(id).setName(v(x.getName())).setDescription(v(x.getDescription())).setType(v(x.getType())).setDifficultyLevel(v(x.getDifficultyLevel())).setDuration(x.getDuration()==null?0:x.getDuration()).build()));}
 @CacheEvict(cacheNames={"programs","program","workouts"}, allEntries=true)
 public Boolean archiveTrainingProgram(Long id){return grpc.archiveTrainingProgram(GetProgramRequest.newBuilder().setIdProgram(id).build()).getSuccess();}
 @CacheEvict(cacheNames={"workouts","workout"}, allEntries=true)
 public WorkoutDto createWorkout(CreateWorkoutInputDto x){return workout(grpc.createWorkout(CreateWorkoutRequest.newBuilder().setProgramId(n(x.getIdTrainingProgram())).setStartDatetime(v(x.getStartDatetime())).setDuration(x.getDuration()==null?0:x.getDuration()).setMaxCapacity(x.getMaxCapacity()==null?0:x.getMaxCapacity()).build()));}
 @CacheEvict(cacheNames={"workouts","workout"}, allEntries=true)
 public WorkoutDto updateWorkout(Long id,UpdateWorkoutInputDto x){return workout(grpc.updateWorkout(UpdateWorkoutRequest.newBuilder().setIdWorkout(id).setStartDatetime(v(x.getStartDatetime())).setDuration(x.getDuration()==null?0:x.getDuration()).setMaxCapacity(x.getMaxCapacity()==null?0:x.getMaxCapacity()).build()));}
 @CacheEvict(cacheNames={"workouts","workout"}, allEntries=true)
 public WorkoutDto changeWorkoutStatus(Long id,String status){return workout(grpc.changeWorkoutStatus(ChangeWorkoutStatusRequest.newBuilder().setIdWorkout(id).setStatus(v(status)).build()));}
 @CacheEvict(cacheNames="progress", allEntries=true)
 public ProgressDto logProgress(LogProgressInputDto x){ProgressItem p=grpc.logProgress(LogProgressRequest.newBuilder().setClientId(n(x.getIdClient())).setBookingId(n(x.getIdBooking())).setValue(x.getValue()==null?0:x.getValue()).setTrainerComment(v(x.getTrainerComment())).build());ProgressDto d=new ProgressDto();d.setIdProgress(p.getIdProgress());d.setDate(p.getDate());d.setValue((float)p.getValue());d.setTrainerComment(p.getTrainerComment());return d;}
}
