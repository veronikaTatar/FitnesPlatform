INSERT INTO trainingprogram (idTrainingProgram, idTrainer, name, description, type, difficulty_level, duration) VALUES
                                                                                                                    (1, 1, 'Йога для начинающих', 'Базовый курс',   'Йога',    'beginner',     60),
                                                                                                                    (2, 2, 'Силовая тренировка',  'Для продвинутых','Силовая', 'advanced',     90),
                                                                                                                    (3, 1, 'Утренняя растяжка',   'Лёгкая практика','Йога',    'intermediate', 45);

INSERT INTO workout (idWorkout, idTrainingProgram, idTrainer, start_datetime, duration, status, max_capacity) VALUES
                                                                                                                  (1, 1, 1, '2026-10-05 18:00:00', 60, 'PLANNED', 15),
                                                                                                                  (2, 1, 1, '2026-10-07 18:00:00', 60, 'PLANNED', 15),
                                                                                                                  (3, 2, 2, '2026-10-06 19:00:00', 90, 'PLANNED', 10),
                                                                                                                  (4, 3, 1, '2026-10-08 08:00:00', 45, 'PLANNED', 20),
                                                                                                                  (5, 2, 2, '2026-10-01 19:00:00', 90, 'COMPLETED', 10);

INSERT INTO progress (idProgress, idClient, idBooking, date, value, trainer_comment) VALUES
                                                                                         (1, 1, 1, '2026-09-20', 72.50, 'Хороший прогресс, держим темп'),
                                                                                         (2, 1, 2, '2026-09-27', 71.80, 'Заметное улучшение');