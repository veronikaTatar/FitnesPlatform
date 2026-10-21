CREATE TABLE trainingprogram (
                                 idTrainingProgram BIGINT AUTO_INCREMENT PRIMARY KEY,
                                 idTrainer         BIGINT NOT NULL,
                                 name              VARCHAR(150) NOT NULL,
                                 description       TEXT,
                                 type              VARCHAR(50),
                                 difficulty_level  VARCHAR(50),
                                 duration          INT NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE workout (
                         idWorkout         BIGINT AUTO_INCREMENT PRIMARY KEY,
                         idTrainingProgram BIGINT NOT NULL,
                         idTrainer         BIGINT NOT NULL,
                         start_datetime    DATETIME NOT NULL,
                         duration          INT NOT NULL,
                         status            VARCHAR(50) NOT NULL,
                         max_capacity      INT NOT NULL,
                         CONSTRAINT fk_workout_program FOREIGN KEY (idTrainingProgram)
                             REFERENCES trainingprogram(idTrainingProgram)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE progress (
                          idProgress      BIGINT AUTO_INCREMENT PRIMARY KEY,
                          idClient        BIGINT NOT NULL,
                          idBooking       BIGINT NOT NULL,
                          date            DATE NOT NULL,
                          value           DECIMAL(10,2),
                          trainer_comment TEXT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;