package spiring.exapmle.userservice.models;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "trainer")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Trainer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idTrainer")
    private Long idTrainer;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idUser", nullable = false, unique = true)
    private User user;

    @Column(name = "specialization", length = 100)
    private String specialization;

    @Column(name = "rating", precision = 3, scale = 2)
    private BigDecimal rating;
}