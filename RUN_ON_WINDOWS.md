# CampusServe — Windows setup and execution

## 1. Requirements
- JDK (install a current JDK and ensure `java -version` and `javac -version` work in a new terminal)
- MySQL Server and MySQL Workbench (or the MySQL command-line client)
- VS Code (optional)

## 2. Create the database
1. Open MySQL Workbench and connect to your local MySQL server.
2. Open and execute `database/schema.sql` first.
3. Open and execute `database/seed_data.sql` second.
4. Open and execute `database/triggers.sql` third.
5. Verify with:

```sql
USE campusserve;
SHOW TABLES;
SELECT UserID, Name, Email, Role FROM `User`;
SELECT * FROM Coordinator;
SELECT * FROM VolunteerProfile;
```

Demo login password is `password123`. Seed accounts:
- admin@campus.edu
- priya@campus.edu
- rahul@campus.edu
- sara@campus.edu

## 3. Configure database credentials
Open `src/campusserve/db/DBConnection.java` and set the JDBC URL, MySQL username, and password to match your local MySQL installation. Do not commit real passwords to a public repository.

## 4. Compile on Windows
Open PowerShell in the project root (the folder containing `src`, `lib`, and `database`). The included `.sh` scripts are for Unix-like shells and may not run in PowerShell directly. Run:

```powershell
$files = Get-ChildItem -Path src -Recurse -Filter *.java | ForEach-Object { $_.FullName }
New-Item -ItemType Directory -Force -Path out | Out-Null
javac -encoding UTF-8 -cp "lib/mysql-connector-j-26.7.0.jar" -d out $files
```

If compilation succeeds, run:

```powershell
java -cp "out;lib/mysql-connector-j-26.7.0.jar" campusserve.Main
```

## 5. Before testing
Test login, event listing, shift registration, cancellation, completion, hours, certificate verification, and coordinator event/roster views. If an error says `Unknown column`, inspect the SQL in the named DAO and compare it with `database/schema.sql`.

## Important
This version changes the volunteer identity to `VolunteerProfile.UserID`, adds the `Coordinator` subtype table, and changes the relevant foreign keys. Always execute the schema on a fresh development database. Do not use these scripts to overwrite an existing database containing important data.
