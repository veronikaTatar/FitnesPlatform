ALTER TABLE workout
    ADD COLUMN booked_count INT NOT NULL DEFAULT 0;

-- Keep the seeded workout database consistent with active bookings in booking_db.
UPDATE workout SET booked_count = CASE idWorkout
    WHEN 1 THEN 2
    WHEN 2 THEN 1
    WHEN 3 THEN 1
    ELSE 0
END;
