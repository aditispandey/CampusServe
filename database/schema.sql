CREATE DATABASE IF NOT EXISTS campusserve;
USE campusserve;

-- 1. Department
CREATE TABLE Department (
    DeptID       INT AUTO_INCREMENT PRIMARY KEY,
    DeptName     VARCHAR(100) NOT NULL UNIQUE
);

-- 2. User (base identity for Volunteer / Coordinator / Administrator)
CREATE TABLE User (
    UserID        INT AUTO_INCREMENT PRIMARY KEY,
    Name          VARCHAR(100) NOT NULL,
    Email         VARCHAR(150) NOT NULL UNIQUE,
    PasswordHash  VARCHAR(255) NOT NULL,
    Role          ENUM('VOLUNTEER','COORDINATOR','ADMIN') NOT NULL,
    DeptID        INT,
    CreatedAt     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (DeptID) REFERENCES Department(DeptID)
        ON DELETE SET NULL ON UPDATE CASCADE
);

-- 3. Club
CREATE TABLE Club (
    ClubID         INT AUTO_INCREMENT PRIMARY KEY,
    ClubName       VARCHAR(100) NOT NULL UNIQUE,
    CoordinatorID  INT NOT NULL,
    FOREIGN KEY (CoordinatorID) REFERENCES User(UserID)
        ON DELETE RESTRICT ON UPDATE CASCADE
);

-- 4. Event
CREATE TABLE Event (
    EventID         INT AUTO_INCREMENT PRIMARY KEY,
    EventName       VARCHAR(150) NOT NULL,
    ClubID          INT NOT NULL,
    StartDate       DATE NOT NULL,
    EndDate         DATE NOT NULL,
    ApprovalStatus  ENUM('PENDING','APPROVED','REJECTED') NOT NULL DEFAULT 'PENDING',
    UpdatedAt       TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (ClubID) REFERENCES Club(ClubID)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CHECK (EndDate >= StartDate)
);

-- 5. ShiftRole
CREATE TABLE ShiftRole (
    ShiftID          INT AUTO_INCREMENT PRIMARY KEY,
    EventID          INT NOT NULL,
    RoleName         VARCHAR(100) NOT NULL,
    StartTime        DATETIME NOT NULL,
    EndTime          DATETIME NOT NULL,
    SlotsAvailable   INT NOT NULL,
    SlotsFilled      INT NOT NULL DEFAULT 0,
    FOREIGN KEY (EventID) REFERENCES Event(EventID)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CHECK (EndTime > StartTime),
    CHECK (SlotsFilled <= SlotsAvailable),
    CHECK (SlotsAvailable > 0)
);

-- 6. VolunteerProfile (1:1 extension of User)
CREATE TABLE VolunteerProfile (
    VolunteerID        INT AUTO_INCREMENT PRIMARY KEY,
    UserID              INT NOT NULL UNIQUE,
    TotalHoursServed    DECIMAL(6,2) NOT NULL DEFAULT 0,
    FOREIGN KEY (UserID) REFERENCES User(UserID)
        ON DELETE CASCADE ON UPDATE CASCADE
);

-- 7. ShiftAssignment (junction table resolving Volunteer <-> ShiftRole N:M)
CREATE TABLE ShiftAssignment (
    AssignmentID   INT AUTO_INCREMENT PRIMARY KEY,
    ShiftID        INT NOT NULL,
    VolunteerID    INT NOT NULL,
    Status         ENUM('REGISTERED','COMPLETED','CANCELLED','NO_SHOW') NOT NULL DEFAULT 'REGISTERED',
    CheckInTime    DATETIME NULL,
    CheckOutTime   DATETIME NULL,
    HoursLogged    DECIMAL(5,2) NOT NULL DEFAULT 0,
    UpdatedAt      TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (ShiftID) REFERENCES ShiftRole(ShiftID)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    FOREIGN KEY (VolunteerID) REFERENCES VolunteerProfile(VolunteerID)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    UNIQUE (ShiftID, VolunteerID)   -- can't register for the same shift twice
);

-- 8. Certificate
CREATE TABLE Certificate (
    CertificateID     INT AUTO_INCREMENT PRIMARY KEY,
    VolunteerID       INT NOT NULL,
    IssueDate         DATE NOT NULL,
    HoursThreshold    INT NOT NULL,
    CertificateCode   VARCHAR(30) NOT NULL UNIQUE,
    FOREIGN KEY (VolunteerID) REFERENCES VolunteerProfile(VolunteerID)
        ON DELETE CASCADE ON UPDATE CASCADE,
    UNIQUE (VolunteerID, HoursThreshold)   -- never issue the same threshold twice
);

-- Indexes for frequently joined/queried columns (DBR-07)
CREATE INDEX idx_user_email ON User(Email);
CREATE INDEX idx_assignment_shift ON ShiftAssignment(ShiftID);
CREATE INDEX idx_assignment_volunteer ON ShiftAssignment(VolunteerID);
CREATE INDEX idx_event_club ON Event(ClubID);
