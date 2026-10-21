CREATE TABLE role (
                      idRole BIGINT AUTO_INCREMENT PRIMARY KEY,
                      name   VARCHAR(50) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE user (
                      idUser   BIGINT AUTO_INCREMENT PRIMARY KEY,
                      idRole   BIGINT       NOT NULL,
                      name     VARCHAR(100) NOT NULL,
                      email    VARCHAR(150) NOT NULL UNIQUE,
                      password VARCHAR(255) NOT NULL,
                      phone    VARCHAR(20),
                      CONSTRAINT fk_user_role FOREIGN KEY (idRole) REFERENCES role(idRole)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE client (
                        idClient      BIGINT AUTO_INCREMENT PRIMARY KEY,
                        idUser        BIGINT NOT NULL UNIQUE,
                        birth_date    DATE,
                        fitness_level VARCHAR(50),
                        CONSTRAINT fk_client_user FOREIGN KEY (idUser) REFERENCES user(idUser) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE trainer (
                         idTrainer      BIGINT AUTO_INCREMENT PRIMARY KEY,
                         idUser         BIGINT NOT NULL UNIQUE,
                         specialization VARCHAR(100),
                         rating         DECIMAL(3,2),
                         CONSTRAINT fk_trainer_user FOREIGN KEY (idUser) REFERENCES user(idUser) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE notification (
                              idNotification BIGINT AUTO_INCREMENT PRIMARY KEY,
                              idUser         BIGINT NOT NULL,
                              text           TEXT NOT NULL,
                              date           DATETIME NOT NULL,
                              is_read        TINYINT(1) NOT NULL DEFAULT 0,
                              type           VARCHAR(50),
                              CONSTRAINT fk_notification_user FOREIGN KEY (idUser) REFERENCES user(idUser) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;