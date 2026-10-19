USE campusserve;
DELIMITER //

-- Add logged hours once when an assignment first becomes COMPLETED.
CREATE TRIGGER trg_complete_shift_assignment
AFTER UPDATE ON ShiftAssignment
FOR EACH ROW
BEGIN
    DECLARE v_total DECIMAL(6,2);
    DECLARE v_existing INT;

    IF NEW.Status = 'COMPLETED' AND OLD.Status <> 'COMPLETED' THEN
        UPDATE VolunteerProfile
        SET TotalHoursServed = TotalHoursServed + NEW.HoursLogged
        WHERE UserID = NEW.VolunteerUserID;

        SELECT TotalHoursServed INTO v_total
        FROM VolunteerProfile WHERE UserID = NEW.VolunteerUserID;

        IF v_total >= 10 THEN
            SELECT COUNT(*) INTO v_existing
            FROM Certificate
            WHERE VolunteerUserID = NEW.VolunteerUserID AND HoursThreshold = 10;

            IF v_existing = 0 THEN
                INSERT INTO Certificate (VolunteerUserID, IssueDate, HoursThreshold, CertificateCode)
                VALUES (NEW.VolunteerUserID, CURDATE(), 10,
                        CONCAT('CS-', NEW.VolunteerUserID, '-', UNIX_TIMESTAMP()));
            END IF;
        END IF;
    END IF;
END//

-- Re-open a slot when an assignment is cancelled.
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
