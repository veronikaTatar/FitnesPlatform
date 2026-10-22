package spiring.exapmle.apigateway.graphql.mutation;

import org.junit.jupiter.api.Test;
import spiring.exapmle.apigateway.client.BookingServiceClient;
import spiring.exapmle.apigateway.dto.BookingDto;
import spiring.exapmle.apigateway.dto.SessionUserDto;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BookingMutationResolverTest {

    @Test
    void passesAuthenticatedUserIdToBookingService() {
        BookingServiceClient bookingServiceClient = mock(BookingServiceClient.class);
        when(bookingServiceClient.bookWorkout(anyLong(), anyLong())).thenReturn(new BookingDto());
        BookingMutationResolver resolver = new BookingMutationResolver(bookingServiceClient);

        resolver.bookWorkout(7L, new SessionUserDto(42L, "client@fit.by", "client"));

        verify(bookingServiceClient).bookWorkout(42L, 7L);
    }
}
