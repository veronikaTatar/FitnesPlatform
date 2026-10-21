package spiring.exapmle.bookingservice.models;

import jakarta.persistence.*;
import lombok.*;
import spiring.exapmle.bookingservice.models.enums.SubscriptionStatus;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDate;

@Entity
@Table(name = "subscription")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idSubscription")
    private Long idSubscription;

    /** Логическая ссылка на user_db.client.idClient */
    @Column(name = "idClient", nullable = false)
    private Long idClient;

    @Column(name = "subscription_type_id", nullable = false)
    private Integer subscriptionTypeId;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(name = "status", nullable = false, length = 50)
    private SubscriptionStatus status;

    @Column(name = "remaining_sessions", nullable = false)
    private Integer remainingSessions;
}