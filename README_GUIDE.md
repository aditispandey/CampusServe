# CampusServe — Updated Windows Setup Guide

This version aligns the SQL schema and Java DAO queries with the finalized subtype design:
- `User` is the parent table.
- `Coordinator(UserID)` and `VolunteerProfile(UserID)` are subtype tables whose primary keys are also foreign keys to `User(UserID)`.
- `ShiftAssignment` implements the many-to-many `AssignedTo` relationship and stores status, check-in/check-out timestamps, and hours logged. `AssignmentID` is retained as a technical primary key because Java workflows refer to individual assignments.

## Requirements
- JDK installed (`java -version` and `javac -version` should work)
- MySQL Server and MySQL Workbench
- VS Code (optional)

## 1. Set up MySQL (fresh database)
In MySQL Workbench, execute these files in this exact order:
1. `database/schema.sql`
2. `database/seed_data.sql`
3. `database/triggers.sql`

The scripts create/use a database named `campusserve`. They include demo records. Use a fresh development database; do not run the schema over a database containing data you need to preserve.

Verify:
```sql
USE campusserve;
SHOW TABLES;
SELECT UserID, Name, Email, Role FROM `User`;
SELECT * FROM Coordinator;
SELECT * FROM VolunteerProfile;
```

Demo accounts (password for each: `password123`):
- `admin@campus.edu`
- `priya@campus.edu`
- `rahul@campus.edu`
- `sara@campus.edu`

## 2. Configure database credentials
Edit `src/campusserve/db/DBConnection.java` and set the JDBC URL, username, and password for your local MySQL installation. If you use the JSP website, also edit `webapp/dbconn.jspf`.

## 3. Compile on Windows
Open PowerShell in the project root (the folder containing `src`, `lib`, and `database`):
```powershell
$files = Get-ChildItem -Path src -Recurse -Filter *.java | ForEach-Object { $_.FullName }
New-Item -ItemType Directory -Force -Path out | Out-Null
javac -encoding UTF-8 -cp "lib/mysql-connector-j-26.7.0.jar" -d out $files
```

## 4. Run
```powershell
java -cp "out;lib/mysql-connector-j-26.7.0.jar" campusserve.Main
```

If compilation reports errors, check that the JDK is installed and that the command is run from the project root. If the application reports a database connection error, check that MySQL is running and `DBConnection.java` credentials match your local setup.

## 5. Suggested smoke tests
1. Log in with each demo account.
2. Check the volunteer and coordinator dashboards.
3. Register for a shift, then cancel it.
4. Test check-in/check-out and completion/hours.
5. Check coordinator event/roster views and certificate verification.

The Java project was compiled with `javac` during packaging. Database execution and live dashboard behavior still need to be tested against your own MySQL installation.
