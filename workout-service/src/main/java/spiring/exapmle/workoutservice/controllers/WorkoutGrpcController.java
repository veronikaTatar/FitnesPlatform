package spiring.exapmle.workoutservice.controllers;

import com.fitness.workout.grpc.*;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.transaction.annotation.Transactional;
import spiring.exapmle.workoutservice.models.Progress;
import spiring.exapmle.workoutservice.models.TrainingProgram;
import spiring.exapmle.workoutservice.models.Workout;
import spiring.exapmle.workoutservice.services.WorkoutService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

@GrpcService
public class WorkoutGrpcController extends WorkoutServiceGrpc.WorkoutServiceImplBase {
    private final WorkoutService workoutService;

    public WorkoutGrpcController(WorkoutService workoutService) {
        this.workoutService = workoutService;
    }

    @Override
    @Transactional(readOnly = true)
    public void getWorkout(GetWorkoutRequest request, StreamObserver<WorkoutResponse> observer) {
        respond(observer, () -> toWorkout(workoutService.getWorkoutById(request.getIdWorkout())));
    }

    @Override
    @Transactional(readOnly = true)
    public void getTrainingProgram(GetProgramRequest request,
                                   StreamObserver<ProgramResponse> observer) {
        respond(observer, () -> toProgram(workoutService.getProgramById(request.getIdProgram())));
    }

    @Override
    public void createTrainingProgram(CreateTrainingProgramRequest request, StreamObserver<ProgramResponse> observer) {
        respond(observer, () -> {
            TrainingProgram program = new TrainingProgram();
            program.setIdTrainer(request.getTrainerId());
            program.setName(request.getName());
            program.setDescription(request.getDescription());
            program.setType(request.getType());
            program.setDifficultyLevel(request.getDifficultyLevel());
            program.setDuration(request.getDuration());
            return toProgram(workoutService.createProgram(program));
        });
    }

    @Override
    public void updateTrainingProgram(UpdateTrainingProgramRequest request, StreamObserver<ProgramResponse> observer) {
        respond(observer, () -> {
            TrainingProgram patch = new TrainingProgram();
            patch.setName(blankToNull(request.getName()));
            patch.setDescription(request.getDescription());
            patch.setType(blankToNull(request.getType()));
            patch.setDifficultyLevel(blankToNull(request.getDifficultyLevel()));
            patch.setDuration(request.getDuration() == 0 ? null : request.getDuration());
            return toProgram(workoutService.updateProgram(request.getIdProgram(), patch));
        });
    }

    @Override
    public void archiveTrainingProgram(GetProgramRequest request, StreamObserver<OperationResponse> observer) {
        respond(observer, () -> {
            workoutService.archiveProgram(request.getIdProgram());
            return OperationResponse.newBuilder().setSuccess(true).build();
        });
    }

    @Override
    public void createWorkout(CreateWorkoutRequest request, StreamObserver<WorkoutResponse> observer) {
        respond(observer, () -> toWorkout(workoutService.createWorkout(
                request.getProgramId(), LocalDateTime.parse(request.getStartDatetime()),
                request.getDuration(), request.getMaxCapacity())));
    }

    @Override
    public void updateWorkout(UpdateWorkoutRequest request, StreamObserver<WorkoutResponse> observer) {
        respond(observer, () -> toWorkout(workoutService.updateWorkout(
                request.getIdWorkout(), request.getStartDatetime().isBlank() ? null
                        : LocalDateTime.parse(request.getStartDatetime()),
                request.getDuration() == 0 ? null : request.getDuration(),
                request.getMaxCapacity() == 0 ? null : request.getMaxCapacity())));
    }

    @Override
    public void changeWorkoutStatus(ChangeWorkoutStatusRequest request, StreamObserver<WorkoutResponse> observer) {
        respond(observer, () -> toWorkout(workoutService.changeWorkoutStatus(
                request.getIdWorkout(), request.getStatus())));
    }

    @Override
    public void logProgress(LogProgressRequest request, StreamObserver<ProgressItem> observer) {
        respond(observer, () -> toProgress(workoutService.logProgress(
                request.getClientId(), request.getBookingId(),
                java.math.BigDecimal.valueOf(request.getValue()),
                blankToNull(request.getTrainerComment()))));
    }

    @Override
    @Transactional(readOnly = true)
    public void getTrainingPrograms(ProgramsRequest request,
                                    StreamObserver<ProgramsResponse> observer) {
        respond(observer, () -> {
            List<TrainingProgram> programs = workoutService.getPrograms(
                    blankToNull(request.getType()),
                    blankToNull(request.getDifficultyLevel()),
                    request.getTrainerId() == 0 ? null : request.getTrainerId());
            Comparator<TrainingProgram> comparator = switch (request.getSortBy()) {
                case "name" -> Comparator.comparing(TrainingProgram::getName,
                        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
                case "duration" -> Comparator.comparing(TrainingProgram::getDuration);
                case "type" -> Comparator.comparing(TrainingProgram::getType,
                        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
                case "id" -> Comparator.comparing(TrainingProgram::getIdTrainingProgram);
                default -> null;
            };
            if (comparator != null) {
                if ("DESC".equalsIgnoreCase(request.getSortOrder())) {
                    comparator = comparator.reversed();
                }
                programs.sort(comparator);
            }
            return ProgramsResponse.newBuilder().addAllPrograms(
                    programs.stream().map(WorkoutGrpcController::toProgram).toList()).build();
        });
    }

    @Override
    @Transactional(readOnly = true)
    public void getWorkouts(WorkoutsRequest request,
                            StreamObserver<WorkoutsResponse> observer) {
        respond(observer, () -> {
            List<Workout> workouts = workoutService.getWorkouts(
                    request.getTrainerId() == 0 ? null : request.getTrainerId(),
                    request.getProgramId() == 0 ? null : request.getProgramId(),
                    parseFrom(request.getDateFrom()), parseTo(request.getDateTo()),
                    blankToNull(request.getStatus()));
            Comparator<Workout> comparator = switch (request.getSortBy()) {
                case "startDatetime" -> Comparator.comparing(Workout::getStartDatetime);
                case "duration" -> Comparator.comparing(Workout::getDuration);
                case "id" -> Comparator.comparing(Workout::getIdWorkout);
                default -> null;
            };
            if (comparator != null) {
                if ("DESC".equalsIgnoreCase(request.getSortOrder())) {
                    comparator = comparator.reversed();
                }
                workouts.sort(comparator);
            }
            return WorkoutsResponse.newBuilder().addAllWorkouts(
                    workouts.stream().map(WorkoutGrpcController::toWorkout).toList()).build();
        });
    }

    @Override
    @Transactional(readOnly = true)
    public void getProgressByClient(ProgressRequest request,
                                   StreamObserver<ProgressListResponse> observer) {
        respond(observer, () -> {
            List<Progress> progress;
            if (request.getDateFrom().isBlank() && request.getDateTo().isBlank()) {
                progress = workoutService.getProgressByClient(request.getClientId());
            } else {
                LocalDate from = request.getDateFrom().isBlank()
                        ? LocalDate.of(1, 1, 1) : LocalDate.parse(request.getDateFrom());
                LocalDate to = request.getDateTo().isBlank()
                        ? LocalDate.of(9999, 12, 31) : LocalDate.parse(request.getDateTo());
                progress = workoutService.getProgressByClientAndPeriod(
                        request.getClientId(), from, to);
            }
            return ProgressListResponse.newBuilder().addAllItems(
                    progress.stream().map(WorkoutGrpcController::toProgress).toList()).build();
        });
    }

    @Override
    @Transactional(readOnly = true)
    public void checkAvailability(CheckAvailabilityRequest request,
                                  StreamObserver<CheckAvailabilityResponse> observer) {
        respond(observer, () -> {
            Workout workout = workoutService.getWorkoutById(request.getIdWorkout());
            int free = Math.max(0, workout.getMaxCapacity() - workout.getBookedCount());
            return CheckAvailabilityResponse.newBuilder()
                    .setAvailable("PLANNED".equals(workout.getStatus()) && free > 0)
                    .setFreeSlots(free)
                    .build();
        });
    }

    @Override
    public void reserveSlot(ReserveSlotRequest request,
                            StreamObserver<ReserveSlotResponse> observer) {
        respond(observer, () -> {
            boolean success = workoutService.reserveSlot(request.getIdWorkout());
            int remaining = success ? Math.max(0, workoutService.getWorkoutById(
                    request.getIdWorkout()).getMaxCapacity()
                    - workoutService.getWorkoutById(request.getIdWorkout()).getBookedCount()) : 0;
            return ReserveSlotResponse.newBuilder()
                    .setSuccess(success).setRemaining(remaining).build();
        });
    }

    @Override
    public void releaseSlot(ReleaseSlotRequest request,
                            StreamObserver<ReleaseSlotResponse> observer) {
        respond(observer, () -> {
            boolean success = workoutService.releaseSlot(request.getIdWorkout());
            int remaining = success ? Math.max(0, workoutService.getWorkoutById(
                    request.getIdWorkout()).getMaxCapacity()
                    - workoutService.getWorkoutById(request.getIdWorkout()).getBookedCount()) : 0;
            return ReleaseSlotResponse.newBuilder()
                    .setSuccess(success).setRemaining(remaining).build();
        });
    }

    private static WorkoutResponse toWorkout(Workout workout) {
        return WorkoutResponse.newBuilder()
                .setIdWorkout(workout.getIdWorkout())
                .setIdTrainingProgram(workout.getTrainingProgram().getIdTrainingProgram())
                .setIdTrainer(workout.getIdTrainer())
                .setStartDatetime(workout.getStartDatetime().toString())
                .setDuration(workout.getDuration())
                .setStatus(toStatus(workout.getStatus()))
                .setMaxCapacity(workout.getMaxCapacity())
                .setBookedCount(workout.getBookedCount())
                .build();
    }

    private static ProgramResponse toProgram(TrainingProgram program) {
        return ProgramResponse.newBuilder()
                .setIdTrainingProgram(program.getIdTrainingProgram())
                .setName(value(program.getName()))
                .setType(value(program.getType()))
                .setDifficultyLevel(value(program.getDifficultyLevel()))
                .setDuration(program.getDuration())
                .setIdTrainer(program.getIdTrainer())
                .setDescription(value(program.getDescription()))
                .build();
    }

    private static ProgressItem toProgress(Progress progress) {
        return ProgressItem.newBuilder()
                .setIdProgress(progress.getIdProgress())
                .setIdClient(progress.getIdClient())
                .setIdBooking(progress.getIdBooking())
                .setDate(progress.getDate().toString())
                .setValue(progress.getValue() == null ? 0.0 : progress.getValue().doubleValue())
                .setTrainerComment(value(progress.getTrainerComment()))
                .build();
    }

    private static WorkoutStatus toStatus(String status) {
        return switch (status.toUpperCase(Locale.ROOT)) {
            case "PLANNED" -> WorkoutStatus.PLANNED;
            case "CANCELLED" -> WorkoutStatus.CANCELLED;
            case "COMPLETED" -> WorkoutStatus.COMPLETED;
            default -> WorkoutStatus.WORKOUT_STATUS_UNKNOWN;
        };
    }

    private static LocalDateTime parseFrom(String value) {
        if (value == null || value.isBlank()) return null;
        return value.length() == 10 ? LocalDate.parse(value).atStartOfDay()
                : LocalDateTime.parse(value);
    }

    private static LocalDateTime parseTo(String value) {
        if (value == null || value.isBlank()) return null;
        return value.length() == 10 ? LocalDate.parse(value).atTime(LocalTime.MAX)
                : LocalDateTime.parse(value);
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
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
        } catch (IllegalStateException exception) {
            observer.onError(Status.FAILED_PRECONDITION
                    .withDescription(exception.getMessage()).asRuntimeException());
        } catch (RuntimeException exception) {
            Status status = exception.getMessage() != null
                    && exception.getMessage().toLowerCase(Locale.ROOT).contains("not found")
                    ? Status.NOT_FOUND : Status.INTERNAL;
            observer.onError(status.withDescription(exception.getMessage()).asRuntimeException());
        }
    }
}
