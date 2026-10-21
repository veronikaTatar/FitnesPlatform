CREATE TABLE attendancestats (
                                 idStat           BIGINT AUTO_INCREMENT PRIMARY KEY,
                                 date             DATE NOT NULL,
                                 workout_count    INT NOT NULL,
                                 booking_count    INT NOT NULL,
                                 attendance_count INT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE revenuestats (
                              idStat               BIGINT AUTO_INCREMENT PRIMARY KEY,
                              date                 DATE NOT NULL,
                              total_revenue        DECIMAL(12,2) NOT NULL,
                              subscription_revenue DECIMAL(12,2) NOT NULL,
                              payment_count        INT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE programpopularity (
                                   idStat            BIGINT AUTO_INCREMENT PRIMARY KEY,
                                   idTrainingProgram BIGINT NOT NULL,
                                   booking_count     INT NOT NULL,
                                   period            VARCHAR(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;