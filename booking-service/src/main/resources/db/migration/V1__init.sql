CREATE TABLE subscription (
                              idSubscription       BIGINT AUTO_INCREMENT PRIMARY KEY,
                              idClient             BIGINT NOT NULL,
                              subscription_type_id INT NOT NULL,
                              start_date           DATE NOT NULL,
                              end_date             DATE NOT NULL,
                              status               VARCHAR(50) NOT NULL,
                              remaining_sessions   INT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE booking (
                         idBooking    BIGINT AUTO_INCREMENT PRIMARY KEY,
                         idClient     BIGINT NOT NULL,
                         idWorkout    BIGINT NOT NULL,
                         status       VARCHAR(50) NOT NULL,
                         booking_date DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE payment (
                         idPayment      BIGINT AUTO_INCREMENT PRIMARY KEY,
                         idClient       BIGINT NOT NULL,
                         amount         DECIMAL(10,2) NOT NULL,
                         payment_date   DATETIME NOT NULL,
                         status         VARCHAR(50) NOT NULL,
                         idSubscription BIGINT,
                         CONSTRAINT fk_payment_subscription FOREIGN KEY (idSubscription)
                             REFERENCES subscription(idSubscription)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;