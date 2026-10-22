package spiring.exapmle.apigateway.config;

import org.junit.jupiter.api.Test;
import spiring.exapmle.apigateway.dto.SessionUserDto;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GraphQlAuthorizationPolicyTest {
    private final GraphQlAuthorizationPolicy policy = new GraphQlAuthorizationPolicy();

    @Test
    void allowsPublicLoginWithoutSession() {
        assertThat(policy.isPublicOperation("mutation { login(input: { email: \"a@fit.by\", password: \"password\" }) { token } }"))
                .isTrue();
    }

    @Test
    void allowsClientBookingButDeniesTrainerMutation() {
        SessionUserDto client = new SessionUserDto(1L, "client@fit.by", "client");

        policy.authorize("mutation { bookWorkout(workoutId: 1) { idBooking } }", client);

        assertThatThrownBy(() -> policy.authorize(
                "mutation { createWorkout(input: { idTrainingProgram: 1, startDatetime: \"2026-10-23T10:00\", duration: 60, maxCapacity: 10 }) { idWorkout } }",
                client))
                .isInstanceOf(GraphQlAccessDeniedException.class)
                .hasMessageContaining("createWorkout");
    }

    @Test
    void allowsTrainerManagementButDeniesAdminAnalytics() {
        SessionUserDto trainer = new SessionUserDto(2L, "trainer@fit.by", "trainer");

        policy.authorize("mutation { archiveTrainingProgram(idTrainingProgram: 1) }", trainer);

        assertThatThrownBy(() -> policy.authorize(
                "query { aggregatedStats(dateFrom: \"2026-01-01\", dateTo: \"2026-01-31\") { totalRevenue } }",
                trainer))
                .isInstanceOf(GraphQlAccessDeniedException.class)
                .hasMessageContaining("aggregatedStats");
    }

    @Test
    void allowsAdminToReadUsersAndAnalytics() {
        SessionUserDto admin = new SessionUserDto(3L, "admin@fit.by", "admin");

        policy.authorize("query { users { idUser } attendanceStats(dateFrom: \"2026-01-01\", dateTo: \"2026-01-31\") { date } }", admin);
    }
}
