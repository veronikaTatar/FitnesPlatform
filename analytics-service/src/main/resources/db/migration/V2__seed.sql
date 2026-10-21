INSERT INTO attendancestats (idStat, date, workout_count, booking_count, attendance_count) VALUES
                                                                                               (1, '2026-10-01', 2, 3, 2),
                                                                                               (2, '2026-10-02', 1, 2, 2),
                                                                                               (3, '2026-10-03', 3, 5, 4);

INSERT INTO revenuestats (idStat, date, total_revenue, subscription_revenue, payment_count) VALUES
                                                                                                (1, '2026-10-01', 175.00, 150.00, 1),
                                                                                                (2, '2026-10-02', 150.00, 150.00, 1),
                                                                                                (3, '2026-10-03',  30.00,   0.00, 1);

INSERT INTO programpopularity (idStat, idTrainingProgram, booking_count, period) VALUES
                                                                                     (1, 1, 12, 'week'),
                                                                                     (2, 2,  8, 'week'),
                                                                                     (3, 3,  5, 'week'),
                                                                                     (4, 1, 45, 'month'),
                                                                                     (5, 2, 32, 'month');