USE campusserve;

INSERT INTO Department (DeptName) VALUES ('Computer Science'), ('Mechanical'), ('Commerce');

-- Demo password for all accounts: password123 (SHA-256 hash used by this project).
INSERT INTO `User` (Name, Email, PasswordHash, Role, DeptID) VALUES
('Admin User',        'admin@campus.edu', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'ADMIN',       1),
('Priya Coordinator', 'priya@campus.edu', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'COORDINATOR', 1),
('Rahul Volunteer',   'rahul@campus.edu', 'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'VOLUNTEER',   1),
('Sara Volunteer',    'sara@campus.edu',  'ef92b778bafe771e89245b89ecbc08a44a4e166c06659911881f383d4473e94f', 'VOLUNTEER',   2);

-- Insert subtype rows after the corresponding User rows.
INSERT INTO Coordinator (UserID) VALUES (2);
INSERT INTO VolunteerProfile (UserID, TotalHoursServed) VALUES (3, 0), (4, 0);

INSERT INTO Club (ClubName, CoordinatorUserID) VALUES ('Tech Fest Committee', 2);

INSERT INTO Event (EventName, ClubID, StartDate, EndDate, ApprovalStatus) VALUES
('Annual Tech Fest',     1, '2026-11-01', '2026-11-03', 'APPROVED'),
('Blood Donation Drive', 1, '2026-12-05', '2026-12-05', 'PENDING');

INSERT INTO ShiftRole (EventID, RoleName, StartTime, EndTime, SlotsAvailable) VALUES
(1, 'Registration Desk', '2026-11-01 09:00:00', '2026-11-01 13:00:00', 5),
(1, 'Stage Setup Crew',  '2026-11-01 07:00:00', '2026-11-01 09:00:00', 3),
(1, 'Food Counter',      '2026-11-02 10:00:00', '2026-11-02 22:00:00', 2),
(1, 'Guest Reception',   '2026-11-01 11:00:00', '2026-11-01 15:00:00', 2);
