package spiring.exapmle.apigateway.graphql.mutation;

import org.junit.jupiter.api.Test;
import com.fitness.user.grpc.ClientResponse;
import com.fitness.user.grpc.GetClientByEmailRequest;
import com.fitness.user.grpc.UserServiceGrpc;
import spiring.exapmle.apigateway.client.BookingServiceClient;
import spiring.exapmle.apigateway.dto.BookingDto;
import spiring.exapmle.apigateway.dto.SessionUserDto;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

class BookingMutationResolverTest {

    @Test
    void resolvesClientProfileByEmailBeforeCallingBookingService() {
        BookingServiceClient bookingServiceClient = mock(BookingServiceClient.class);
        UserServiceGrpc.UserServiceBlockingStub userService = mock(UserServiceGrpc.UserServiceBlockingStub.class);
        when(bookingServiceClient.bookWorkout(anyLong(), anyLong())).thenReturn(new BookingDto());
        when(userService.getClientByEmail(any(GetClientByEmailRequest.class)))
                .thenReturn(ClientResponse.newBuilder().setIdClient(12L).build());
        BookingMutationResolver resolver = new BookingMutationResolver(bookingServiceClient, userService);

        resolver.bookWorkout(7L, new SessionUserDto(42L, "client@fit.by", "client"));

        verify(userService).getClientByEmail(GetClientByEmailRequest.newBuilder()
                .setEmail("client@fit.by").build());
        verify(bookingServiceClient).bookWorkout(12L, 7L);
    }
}
