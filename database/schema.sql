CREATE DATABASE IF NOT EXISTS campusserve CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE campusserve;

CREATE TABLE Department (
    DeptID INT AUTO_INCREMENT PRIMARY KEY,
    DeptName VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE `User` (
    UserID INT AUTO_INCREMENT PRIMARY KEY,
    Name VARCHAR(100) NOT NULL,
    Email VARCHAR(150) NOT NULL UNIQUE,
    PasswordHash VARCHAR(255) NOT NULL,
    Role ENUM('VOLUNTEER','COORDINATOR','ADMIN') NOT NULL,
    DeptID INT NULL,
    CreatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_department FOREIGN KEY (DeptID)
        REFERENCES Department(DeptID) ON DELETE SET NULL ON UPDATE CASCADE
);

-- Subtype table: UserID is both PK and FK to User.
CREATE TABLE Coordinator (
    UserID INT PRIMARY KEY,
    CONSTRAINT fk_coordinator_user FOREIGN KEY (UserID)
        REFERENCES `User`(UserID) ON DELETE CASCADE ON UPDATE CASCADE
);

CREATE TABLE Club (
    ClubID INT AUTO_INCREMENT PRIMARY KEY,
    ClubName VARCHAR(100) NOT NULL UNIQUE,
    CoordinatorUserID INT NOT NULL,
    CONSTRAINT fk_club_coordinator FOREIGN KEY (CoordinatorUserID)
        REFERENCES Coordinator(UserID) ON DELETE RESTRICT ON UPDATE CASCADE
);

CREATE TABLE Event (
    EventID INT AUTO_INCREMENT PRIMARY KEY,
    EventName VARCHAR(150) NOT NULL,
    ClubID INT NOT NULL,
    StartDate DATE NOT NULL,
    EndDate DATE NOT NULL,
    ApprovalStatus ENUM('PENDING','APPROVED','REJECTED') NOT NULL DEFAULT 'PENDING',
    UpdatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_event_club FOREIGN KEY (ClubID)
        REFERENCES Club(ClubID) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT chk_event_dates CHECK (EndDate >= StartDate)
);

CREATE TABLE ShiftRole (
    ShiftID INT AUTO_INCREMENT PRIMARY KEY,
    EventID INT NOT NULL,
    RoleName VARCHAR(100) NOT NULL,
    StartTime DATETIME NOT NULL,
    EndTime DATETIME NOT NULL,
    SlotsAvailable INT NOT NULL,
    SlotsFilled INT NOT NULL DEFAULT 0,
    CONSTRAINT fk_shift_event FOREIGN KEY (EventID)
        REFERENCES Event(EventID) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT chk_shift_times CHECK (EndTime > StartTime),
    CONSTRAINT chk_slots CHECK (SlotsAvailable > 0 AND SlotsFilled >= 0 AND SlotsFilled <= SlotsAvailable)
);

-- Subtype table: UserID is both PK and FK to User.
CREATE TABLE VolunteerProfile (
    UserID INT PRIMARY KEY,
    TotalHoursServed DECIMAL(6,2) NOT NULL DEFAULT 0,
    CONSTRAINT fk_volunteer_user FOREIGN KEY (UserID)
        REFERENCES `User`(UserID) ON DELETE CASCADE ON UPDATE CASCADE
);

-- Relational junction table implementing the AssignedTo M:N relationship.
-- AssignmentID is retained because existing Java workflows refer to it.
CREATE TABLE ShiftAssignment (
    AssignmentID INT AUTO_INCREMENT PRIMARY KEY,
    ShiftID INT NOT NULL,
    VolunteerUserID INT NOT NULL,
    Status ENUM('REGISTERED','COMPLETED','CANCELLED','NO_SHOW') NOT NULL DEFAULT 'REGISTERED',
    CheckInTime DATETIME NULL,
    CheckOutTime DATETIME NULL,
    HoursLogged DECIMAL(5,2) NOT NULL DEFAULT 0,
    UpdatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_assignment_shift FOREIGN KEY (ShiftID)
        REFERENCES ShiftRole(ShiftID) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_assignment_volunteer FOREIGN KEY (VolunteerUserID)
        REFERENCES VolunteerProfile(UserID) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT uq_shift_volunteer UNIQUE (ShiftID, VolunteerUserID),
    CONSTRAINT chk_assignment_hours CHECK (HoursLogged >= 0),
    CONSTRAINT chk_assignment_check_times CHECK (CheckOutTime IS NULL OR (CheckInTime IS NOT NULL AND CheckOutTime >= CheckInTime))
);

CREATE TABLE Certificate (
    CertificateID INT AUTO_INCREMENT PRIMARY KEY,
    VolunteerUserID INT NOT NULL,
    IssueDate DATE NOT NULL,
    HoursThreshold INT NOT NULL,
    CertificateCode VARCHAR(30) NOT NULL UNIQUE,
    CONSTRAINT fk_certificate_volunteer FOREIGN KEY (VolunteerUserID)
        REFERENCES VolunteerProfile(UserID) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT uq_volunteer_threshold UNIQUE (VolunteerUserID, HoursThreshold),
    CONSTRAINT chk_certificate_threshold CHECK (HoursThreshold >= 0)
);

CREATE INDEX idx_assignment_shift ON ShiftAssignment(ShiftID);
CREATE INDEX idx_assignment_volunteer ON ShiftAssignment(VolunteerUserID);
CREATE INDEX idx_event_club ON Event(ClubID);
