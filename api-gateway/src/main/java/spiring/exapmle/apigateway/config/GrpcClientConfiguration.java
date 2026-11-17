package spiring.exapmle.apigateway.config;

import com.fitness.analytics.grpc.AnalyticsServiceGrpc;
import com.fitness.booking.grpc.BookingServiceGrpc;
import com.fitness.user.grpc.UserServiceGrpc;
import com.fitness.workout.grpc.WorkoutServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcClientConfiguration {
    @Bean(destroyMethod = "shutdownNow")
    public ManagedChannel userServiceChannel(
            @Value("${grpc.client.userService.address:static://localhost:9091}") String address) {
        return channel(address);
    }

    @Bean(destroyMethod = "shutdownNow")
    public ManagedChannel workoutServiceChannel(
            @Value("${grpc.client.workoutService.address:static://localhost:9192}") String address) {
        return channel(address);
    }

    @Bean(destroyMethod = "shutdownNow")
    public ManagedChannel bookingServiceChannel(
            @Value("${grpc.client.bookingService.address:static://localhost:9093}") String address) {
        return channel(address);
    }

    @Bean(destroyMethod = "shutdownNow")
    public ManagedChannel analyticsServiceChannel(
            @Value("${grpc.client.analyticsService.address:static://localhost:9094}") String address) {
        return channel(address);
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

    private static ManagedChannel channel(String address) {
        String target = address.startsWith("static://")
                ? address.substring("static://".length()) : address;
        int separator = target.lastIndexOf(':');
        if (separator < 1) {
            throw new IllegalArgumentException("Expected gRPC address host:port, got: " + address);
        }
        return ManagedChannelBuilder.forAddress(
                        target.substring(0, separator),
                        Integer.parseInt(target.substring(separator + 1)))
                .usePlaintext()
                .build();
    }
}
