INSERT INTO subscription (idSubscription, idClient, subscription_type_id, start_date, end_date, status, remaining_sessions) VALUES
                                                                                                                                (1, 1, 1, '2026-09-01', '2026-12-01', 'ACTIVE',    10),
                                                                                                                                (2, 2, 1, '2026-09-15', '2026-12-15', 'ACTIVE',     8),
                                                                                                                                (3, 1, 2, '2026-06-01', '2026-09-01', 'COMPLETED',  0);

INSERT INTO booking (idBooking, idClient, idWorkout, status, booking_date) VALUES
                                                                               (1, 1, 1, 'ACTIVE',    '2026-10-01 10:00:00'),
                                                                               (2, 1, 2, 'ACTIVE',    '2026-10-02 10:00:00'),
                                                                               (3, 2, 3, 'ACTIVE',    '2026-10-02 11:30:00'),
                                                                               (4, 1, 5, 'CANCELLED', '2026-09-30 09:00:00'),
                                                                               (5, 2, 1, 'ACTIVE',    '2026-10-03 12:00:00');

INSERT INTO payment (idPayment, idClient, amount, payment_date, status, idSubscription) VALUES
                                                                                            (1, 1, 150.00, '2026-09-01 12:00:00', 'CONFIRMED', 1),
                                                                                            (2, 2, 150.00, '2026-09-15 12:30:00', 'CONFIRMED', 2),
                                                                                            (3, 1,  25.00, '2026-10-01 10:00:00', 'CONFIRMED', NULL),
                                                                                            (4, 2,  30.00, '2026-10-02 11:30:00', 'PENDING',   NULL);