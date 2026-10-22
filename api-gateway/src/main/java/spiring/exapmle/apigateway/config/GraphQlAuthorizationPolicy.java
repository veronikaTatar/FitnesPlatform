package spiring.exapmle.apigateway.config;

import graphql.language.Document;
import graphql.language.Field;
import graphql.language.OperationDefinition;
import graphql.language.Selection;
import graphql.parser.Parser;
import org.springframework.stereotype.Component;
import spiring.exapmle.apigateway.dto.SessionUserDto;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Component
public class GraphQlAuthorizationPolicy {
    private static final Set<String> ALL_AUTHENTICATED = Set.of("client", "trainer", "admin");
    private static final Map<String, Set<String>> OPERATION_ROLES = Map.ofEntries(
            Map.entry("me", ALL_AUTHENTICATED),
            Map.entry("trainingPrograms", ALL_AUTHENTICATED),
            Map.entry("trainingProgram", ALL_AUTHENTICATED),
            Map.entry("workout", ALL_AUTHENTICATED),
            Map.entry("workouts", ALL_AUTHENTICATED),
            Map.entry("myNotifications", ALL_AUTHENTICATED),
            Map.entry("logout", ALL_AUTHENTICATED),
            Map.entry("updateProfile", ALL_AUTHENTICATED),
            Map.entry("markNotificationRead", ALL_AUTHENTICATED),
            Map.entry("markAllNotificationsRead", ALL_AUTHENTICATED),

            Map.entry("myBookings", Set.of("client")),
            Map.entry("bookWorkout", Set.of("client")),
            Map.entry("cancelBooking", Set.of("client")),
            Map.entry("mySubscriptions", Set.of("client")),
            Map.entry("subscription", Set.of("client")),
            Map.entry("myPayments", Set.of("client")),
            Map.entry("createSubscription", Set.of("client")),
            Map.entry("myProgress", Set.of("client")),

            Map.entry("clientProgress", Set.of("trainer", "admin")),
            Map.entry("createTrainingProgram", Set.of("trainer", "admin")),
            Map.entry("updateTrainingProgram", Set.of("trainer", "admin")),
            Map.entry("archiveTrainingProgram", Set.of("trainer", "admin")),
            Map.entry("createWorkout", Set.of("trainer", "admin")),
            Map.entry("updateWorkout", Set.of("trainer", "admin")),
            Map.entry("changeWorkoutStatus", Set.of("trainer", "admin")),
            Map.entry("logProgress", Set.of("trainer", "admin")),

            Map.entry("user", Set.of("admin")),
            Map.entry("users", Set.of("admin")),
            Map.entry("bookings", Set.of("admin")),
            Map.entry("booking", Set.of("admin")),
            Map.entry("attendanceStats", Set.of("admin")),
            Map.entry("revenueStats", Set.of("admin")),
            Map.entry("programPopularity", Set.of("admin")),
            Map.entry("aggregatedStats", Set.of("admin")),
            Map.entry("updateUserRole", Set.of("admin")),
            Map.entry("blockUser", Set.of("admin")),
            Map.entry("changeSubscriptionStatus", Set.of("admin")),
            Map.entry("registerPayment", Set.of("admin"))
    );

    public boolean isPublicOperation(String source) {
        OperationDefinition operation = singleOperation(source);
        return operation.getOperation() == OperationDefinition.Operation.MUTATION
                && operation.getSelectionSet().getSelections().size() == 1
                && operation.getSelectionSet().getSelections().getFirst() instanceof Field field
                && (field.getName().equals("register") || field.getName().equals("login"));
    }

    public void authorize(String source, SessionUserDto user) {
        OperationDefinition operation = singleOperation(source);
        String role = user.role().toLowerCase(Locale.ROOT);

        for (Selection<?> selection : operation.getSelectionSet().getSelections()) {
            if (!(selection instanceof Field field)) {
                throw new GraphQlAccessDeniedException("GraphQL fragments are not supported in protected operations");
            }
            Set<String> permittedRoles = OPERATION_ROLES.get(field.getName());
            if (permittedRoles == null || !permittedRoles.contains(role)) {
                throw new GraphQlAccessDeniedException(
                        "Role '" + role + "' is not allowed to execute operation '" + field.getName() + "'");
            }
        }
    }

    private OperationDefinition singleOperation(String source) {
        try {
            Document document = new Parser().parseDocument(source);
            if (document.getDefinitions().size() != 1
                    || !(document.getDefinitions().getFirst() instanceof OperationDefinition operation)) {
                throw new GraphQlAccessDeniedException("Request must contain one GraphQL operation");
            }
            return operation;
        } catch (GraphQlAccessDeniedException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new GraphQlAccessDeniedException("Invalid GraphQL operation");
        }
    }
}
