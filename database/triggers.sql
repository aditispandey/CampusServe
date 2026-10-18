USE campusserve;
DELIMITER //

-- Trigger 1: when a shift assignment is marked COMPLETED, add hours and
-- auto-issue a certificate once the volunteer crosses the 10-hour threshold.
CREATE TRIGGER trg_complete_shift_assignment
AFTER UPDATE ON ShiftAssignment
FOR EACH ROW
BEGIN
    DECLARE v_total DECIMAL(6,2);
    DECLARE v_existing INT;

    IF NEW.Status = 'COMPLETED' AND OLD.Status <> 'COMPLETED' THEN

        UPDATE VolunteerProfile
        SET TotalHoursServed = TotalHoursServed + NEW.HoursLogged
        WHERE VolunteerID = NEW.VolunteerID;

        SELECT TotalHoursServed INTO v_total
        FROM VolunteerProfile WHERE VolunteerID = NEW.VolunteerID;

        IF v_total >= 10 THEN
            SELECT COUNT(*) INTO v_existing
            FROM Certificate
            WHERE VolunteerID = NEW.VolunteerID AND HoursThreshold = 10;

            IF v_existing = 0 THEN
                INSERT INTO Certificate (VolunteerID, IssueDate, HoursThreshold, CertificateCode)
                VALUES (
                    NEW.VolunteerID,
                    CURDATE(),
                    10,
                    CONCAT('CS-', NEW.VolunteerID, '-', UNIX_TIMESTAMP())
                );
            END IF;
        END IF;
    END IF;
END//

-- Trigger 2: when an assignment is cancelled, re-open the slot.
CREATE TRIGGER trg_cancel_shift_assignment
AFTER UPDATE ON ShiftAssignment
FOR EACH ROW
BEGIN
    IF NEW.Status = 'CANCELLED' AND OLD.Status <> 'CANCELLED' THEN
        UPDATE ShiftRole
        SET SlotsFilled = SlotsFilled - 1
        WHERE ShiftID = NEW.ShiftID AND SlotsFilled > 0;
    END IF;
END//

DELIMITER ;
