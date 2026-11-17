package spiring.exapmle.apigateway.config;

import com.fitness.analytics.grpc.AnalyticsServiceGrpc;
import com.fitness.booking.grpc.BookingServiceGrpc;
import com.fitness.user.grpc.UserServiceGrpc;
import com.fitness.workout.grpc.WorkoutServiceGrpc;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.MethodDescriptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Configuration
public class GrpcClientConfiguration {
    @Value("${resilience.grpc.deadline-ms:2500}")
    private long deadlineMs;

    @Bean(destroyMethod = "shutdownNow")
    public ManagedChannel userServiceChannel(
            @Value("${grpc.client.userService.address:static://localhost:9091}") String address) {
        return channel(address, "user_service.UserService", List.of(
                "GetUser", "GetClient", "GetClientByUser", "GetClientByEmail",
                "GetTrainer", "GetTrainerByUser", "CheckUserExists", "GetUsers",
                "GetUsersByIds"));
    }

    @Bean(destroyMethod = "shutdownNow")
    public ManagedChannel workoutServiceChannel(
            @Value("${grpc.client.workoutService.address:static://localhost:9192}") String address) {
        return channel(address, "workout_service.WorkoutService", List.of(
                "GetWorkout", "GetTrainingProgram", "GetTrainingPrograms", "GetWorkouts",
                "GetProgressByClient", "CheckAvailability"));
    }

    @Bean(destroyMethod = "shutdownNow")
    public ManagedChannel bookingServiceChannel(
            @Value("${grpc.client.bookingService.address:static://localhost:9093}") String address) {
        return channel(address, "booking_service.BookingService", List.of(
                "GetBookings", "GetBooking", "GetClientBookings", "GetClientSubscriptions",
                "GetSubscription", "GetActiveSubscription", "GetPayment", "GetClientPayments"));
    }

    @Bean(destroyMethod = "shutdownNow")
    public ManagedChannel analyticsServiceChannel(
            @Value("${grpc.client.analyticsService.address:static://localhost:9094}") String address) {
        return channel(address, "analytics_service.AnalyticsService", List.of(
                "GetAttendanceStats", "GetRevenueStats", "GetProgramPopularity",
                "GetAggregatedStats"));
    }

    @Bean
    public UserServiceGrpc.UserServiceBlockingStub userServiceBlockingStub(
            @Qualifier("userServiceChannel") ManagedChannel userServiceChannel) {
        return UserServiceGrpc.newBlockingStub(userServiceChannel);
    }

    @Bean
    public WorkoutServiceGrpc.WorkoutServiceBlockingStub workoutServiceBlockingStub(
            @Qualifier("workoutServiceChannel") ManagedChannel workoutServiceChannel) {
        return WorkoutServiceGrpc.newBlockingStub(workoutServiceChannel);
    }

    @Bean
    public BookingServiceGrpc.BookingServiceBlockingStub bookingServiceBlockingStub(
            @Qualifier("bookingServiceChannel") ManagedChannel bookingServiceChannel) {
        return BookingServiceGrpc.newBlockingStub(bookingServiceChannel);
    }

    @Bean
    public AnalyticsServiceGrpc.AnalyticsServiceBlockingStub analyticsServiceBlockingStub(
            @Qualifier("analyticsServiceChannel") ManagedChannel analyticsServiceChannel) {
        return AnalyticsServiceGrpc.newBlockingStub(analyticsServiceChannel);
    }

    private ManagedChannel channel(String address, String serviceName, List<String> retryableMethods) {
        String target = address.startsWith("static://")
                ? address.substring("static://".length()) : address;
        int separator = target.lastIndexOf(':');
        if (separator < 1) {
            throw new IllegalArgumentException("Expected gRPC address host:port, got: " + address);
        }
        ManagedChannelBuilder<?> builder = ManagedChannelBuilder.forAddress(
                        target.substring(0, separator),
                        Integer.parseInt(target.substring(separator + 1)))
                .usePlaintext()
                .intercept(deadlineInterceptor(deadlineMs));
        if (!retryableMethods.isEmpty()) {
            List<Map<String, Object>> methodConfigs = retryableMethods.stream()
                    .map(method -> Map.<String, Object>of(
                            "name", List.of(Map.of("service", serviceName, "method", method)),
                            "retryPolicy", Map.of(
                                    "maxAttempts", "3",
                                    "initialBackoff", "0.1s",
                                    "maxBackoff", "0.5s",
                                    "backoffMultiplier", 2.0,
                                    "retryableStatusCodes", List.of("UNAVAILABLE"))))
                    .toList();
            builder.enableRetry().defaultServiceConfig(Map.of("methodConfig", methodConfigs));
        }
        return builder.build();
    }

    private static ClientInterceptor deadlineInterceptor(long deadlineMs) {
        return new ClientInterceptor() {
            @Override
            public <RequestT, ResponseT> ClientCall<RequestT, ResponseT> interceptCall(
                    MethodDescriptor<RequestT, ResponseT> method,
                    CallOptions callOptions,
                    Channel next) {
                return next.newCall(method, callOptions.withDeadlineAfter(
                        deadlineMs, java.util.concurrent.TimeUnit.MILLISECONDS));
            }
        };
    }
}
