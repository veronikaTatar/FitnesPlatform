INSERT INTO role (idRole, name) VALUES
                                    (1, 'client'),
                                    (2, 'trainer'),
                                    (3, 'admin');

INSERT INTO user (idUser, idRole, name, email, password, phone) VALUES
                                                                    (1, 1, 'Иванов Иван',   'ivanov@fit.by', '$2a$10$abcdefghijklmnopqrstuv', '+375291112233'),
                                                                    (2, 1, 'Кузнецова Анна','anna@fit.by',   '$2a$10$abcdefghijklmnopqrstuv', '+375292223344'),
                                                                    (3, 2, 'Петров Пётр',   'petrov@fit.by', '$2a$10$abcdefghijklmnopqrstuv', '+375293334455'),
                                                                    (4, 2, 'Сидорова Мария','maria@fit.by',  '$2a$10$abcdefghijklmnopqrstuv', '+375294445566'),
                                                                    (5, 3, 'Админов Олег',  'admin@fit.by',  '$2a$10$abcdefghijklmnopqrstuv', '+375295556677');

INSERT INTO client (idClient, idUser, birth_date, fitness_level) VALUES
                                                                     (1, 1, '1995-03-15', 'beginner'),
                                                                     (2, 2, '1992-07-22', 'intermediate');

INSERT INTO trainer (idTrainer, idUser, specialization, rating) VALUES
                                                                    (1, 3, 'Йога',      4.80),
                                                                    (2, 4, 'Силовая',   4.60);

INSERT INTO notification (idNotification, idUser, text, date, is_read, type) VALUES
                                                                                 (1, 1, 'Добро пожаловать в фитнес-платформу!', NOW(), 0, 'SYSTEM'),
                                                                                 (2, 1, 'Напоминание: занятие завтра в 18:00',  NOW(), 0, 'WORKOUT');