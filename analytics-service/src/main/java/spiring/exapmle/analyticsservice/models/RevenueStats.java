package spiring.exapmle.analyticsservice.models;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "revenuestats")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class RevenueStats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idStat")
    private Long idStat;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "total_revenue", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalRevenue;

    @Column(name = "subscription_revenue", nullable = false, precision = 12, scale = 2)
    private BigDecimal subscriptionRevenue;

    @Column(name = "payment_count", nullable = false)
    private Integer paymentCount;
}