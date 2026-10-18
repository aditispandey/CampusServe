# CampusServe – Java Mini Project (Mac + VS Code)

One real application (Swing + JDBC + MySQL + a small JSP site). All 30 lab concepts are **used inside it** – nothing is a separate demo file.
Section 5 tells you exactly which file shows which concept (useful for your viva).

---

## 1. Do this once

**A. Unzip.** Double-click `CampusServe.zip`, move the `CampusServe` folder to your Desktop. Inside you should directly see `src`, `lib`, `database`, `webapp`.

**B. MySQL driver jar.**
1. Go to dev.mysql.com/downloads/connector/j → choose **Platform Independent** → download the ZIP → "No thanks, just start my download".
2. Unzip it, find `mysql-connector-j-8.x.x.jar`.
3. Copy that jar into **two** folders: `CampusServe/lib` and `CampusServe/webapp/WEB-INF/lib`.

**C. Create the database (MySQL Workbench).**
1. Connect to your local server.
2. File → Open SQL Script → `CampusServe/database/schema.sql` → click the ⚡ lightning icon.
3. Do the same for `triggers.sql`, then `seed_data.sql` (this order).
4. Test: run `SELECT Name, Role FROM campusserve.User;` → you should see 4 users.

**D. Put your MySQL password in the code (2 files).** Use the same password you use in Workbench.
- `src/campusserve/db/DBConnection.java` → change `USER` and `PASSWORD`
- `webapp/dbconn.jspf` → change `"root", "root"` (only needed for the JSP part)

**E. Open in VS Code.**
1. Install the extension **Extension Pack for Java** (Extensions icon on the left).
2. File → Open Folder → choose `CampusServe` → click "Yes, I trust".
3. Wait ~30 seconds for the Java extension to load. (`.vscode/settings.json` is already inside, so it finds `src` and the jar.)

---

## 2. Run the application

**Terminal in VS Code:** menu *Terminal → New Terminal* (opens in the project folder). Run once:
```
chmod +x compile.sh run.sh
./compile.sh
```
(`./compile.sh` is plain `javac` on the command line – this is **Lab 1**, compiling and running Java without an IDE.)

**Start the app:**
```
./run.sh campusserve.Main
```
It prints a banner, checks MySQL, and opens the Login window.
(You can also open `src/campusserve/Main.java` in VS Code and click **Run** above `main`.)

**Start the server (needed for 2 buttons: socket + RMI).** Open a **second terminal** (the **+** in the terminal panel) and run:
```
./run.sh campusserve.network.CampusServer
```
Leave it running. If you skip it, those two buttons simply say "Cannot reach CampusServer".

**Logins (password for all = `password123`)**

| Role | Email |
|---|---|
| Volunteer | rahul@campus.edu, sara@campus.edu |
| Coordinator | priya@campus.edu |
| Admin | admin@campus.edu |

---

## 3. Demo script (follow this order)

1. **Login window** → click **Notice Board (Applet)** → shows open shifts (the applet).
2. **Admin** (`admin@campus.edu`): select "Blood Donation Drive" → **Approve**. Click **View Activity Log** to see the log file.
3. **Volunteer** (`rahul@campus.edu`):
   - Open Shifts tab → select *Registration Desk* → **Register** → confirm. Shows "hours after completion".
   - Select *Guest Reception* (overlaps) → Register → **DoubleBookingException** message.
   - Select *Food Counter* → **Check Slots on Server** → socket reply.
   - Register for *Food Counter* (12 hours).
   - **Monitor Thread Status** button → shows the background thread state (TIMED_WAITING).
   - My Shifts tab → **Export My Report** → creates `reports/Rahul_Volunteer_report.txt`.
4. **Coordinator** (`priya@campus.edu`):
   - My Events → **Create Event**, then **Add Shift to Selected Event**.
   - Shifts tab → **Duplicate Selected Shift** (copy constructor) or **Delete**.
   - **Browse Volunteers** → First / Previous / Next / Last.
   - Roster tab → select Rahul's *Food Counter* → **Mark Complete**.
5. **Volunteer again** (Rahul) → Refresh: 12 hours + a certificate code (the database trigger did it).
6. **Admin** → **Verify Certificate (RMI)** → paste the code → "VALID – issued to Rahul…".

---

## 4. JSP part (Labs 29 & 30) – Tomcat on Mac

1. Download **Apache Tomcat 9** (the `.tar.gz`) from tomcat.apache.org, double-click to extract.
2. Copy the project's `webapp` folder into Tomcat's `webapps` folder and **rename it `campusserve`**
   (so you have `.../webapps/campusserve/index.jsp`). Make sure the MySQL jar is inside `campusserve/WEB-INF/lib`.
3. In Terminal:
   ```
   cd /path/to/tomcat/bin
   chmod +x *.sh
   export JAVA_HOME=$(/usr/libexec/java_home)
   ./startup.sh
   ```
4. Open in your browser:
   - `http://localhost:8080/campusserve/index.jsp` → **Lab 29** (JSP basics)
   - `http://localhost:8080/campusserve/events.jsp` → **Lab 30** (Create / Read / Update / Delete events)
5. Stop with `./shutdown.sh`.

---

## 5. Where each lab concept is used

| # | Concept | Where in the project |
|---|---|---|
| 1 | CLI, variables, methods | `./compile.sh` + `./run.sh campusserve.Main`; `Main.java` (variables `APP_NAME`, methods `printBanner`, `databaseReachable`) |
| 2 | Default / parameterized / copy constructors | `model/ShiftRole.java` (all 3) and `model/Volunteer.java` (all 3). Default: `ShiftDAO.mapShift`. Parameterized: Coordinator "Add Shift". Copy: "Duplicate Shift" and the hours preview when registering |
| 3 | Overloading + static | Overloaded: `Volunteer.addHours`, `Rules.formatHours`, `Validator.require`, `Messages.show`. Static: `Volunteer.volunteerCount`, `Messages.shownCount`, all of `Rules`/`Validator` |
| 4 | Wrapper classes | `Validator.parseInt` (`Integer.valueOf`), `BaseDashboard.selectedId` (`(Integer)` cast + unboxing), `Double hours` in `VolunteerDashboard.refresh`, `Integer.compare` |
| 5 | ArrayList, Vector, StringBuffer | ArrayList: `ShiftDAO.getOpenShifts`. Vector: `BaseDashboard.fill` (table rows). StringBuffer: `ReportBuilder`, `ActivityLog.readAll`, `NoticeApplet` |
| 6 | Inner + anonymous class | Inner: `LoginFrame.LoginListener`, `VolunteerDashboard.RegisterAction`. Anonymous: logout button in `BaseDashboard`, `Comparator` in `VolunteerDashboard.refresh`, `newModel` |
| 7 | Inheritance + interfaces | `User` → `Volunteer`/`Coordinator`/`Administrator`; `BaseDashboard` → 3 dashboards; interface `Reportable` |
| 8 | `super` | `super(...)` in every constructor; `super.getReport()` in `Volunteer`; `super.getBasicInfo()` in `Coordinator`; `super(user.getDashboardTitle())` in `BaseDashboard` |
| 9 | `final` | `final` class `Rules`, `DBConnection`; `final` method `User.getRole()`, `BaseDashboard.init()`; `final` constant `Rules.CERTIFICATE_THRESHOLD` |
| 10 | Abstract class/methods | `User.getDashboardTitle()`, `BaseDashboard.buildContent()` |
| 11 | Implementing interfaces | `Reportable` (User, ShiftRole), `ActionListener`, `Runnable`, `AutoCloseable` (`VolunteerCursor`), `Remote` (`CertificateService`) |
| 12 | User-defined packages | `campusserve.db / model / dao / ui / util / exceptions / tasks / network` |
| 13 | File reading/writing | `util/ActivityLog.java` (append + read log), `tasks/ExportTask.java` (writes report file) |
| 14 | try/catch, multiple, nested | Nested: `CoordinatorDashboard.addShift`, `ActivityLog.log`. Multiple catch: `RegisterAction`, `ExportTask` |
| 15 | finally, throw, throws | `finally`: `ShiftDAO.registerForShift`, `ActivityLog`. `throw`: `Validator`, `EventDAO.deletePendingEvent`. `throws`: all DAO methods |
| 16 | User-defined exceptions | `exceptions/DoubleBookingException`, `InsufficientSlotsException`, `InvalidInputException` |
| 17 | Swing + Applet | Swing everywhere; applet = `ui/NoticeApplet.java` (opened from Login window) |
| 18 | Button click events | every button (`addActionListener`) |
| 19 | JDBC SELECT | `ShiftDAO.getOpenShifts`, `EventDAO.getAllEvents` |
| 20 | INSERT / UPDATE / DELETE | INSERT: `createEvent`, `createShiftRole`, `registerVolunteer`. UPDATE: `updateApprovalStatus`, `markComplete`. DELETE: `deleteShift`, `deletePendingEvent` |
| 21 | DriverManager, Connection, Statement | `db/DBConnection` (DriverManager → Connection); `Statement` in `getOpenShifts`, `getAllEvents` |
| 22 | PreparedStatement, ResultSet | all DAO methods with `?` parameters |
| 23 | ResultSet navigation | `dao/VolunteerCursor` + `ui/VolunteerBrowserDialog` (first/next/previous/last/getRow) |
| 24 | GUI + JDBC | the whole app (`ui` package + `dao` package) |
| 25 | Thread class + life cycle | `tasks/SlotMonitor extends Thread` (NEW → RUNNABLE → TIMED_WAITING → TERMINATED, logged and shown by "Monitor Thread Status") |
| 26 | Runnable | `tasks/ExportTask`, `network/SlotServer.ClientHandler`, the monitor's tick `Runnable` |
| 27 | Sockets | `network/SlotServer` + `SlotClient` ("Check Slots on Server" button) |
| 28 | RMI | `CertificateService` (remote interface), `CertificateServiceImpl`, `CampusServer`, `CertificateClient` (Admin "Verify Certificate") |
| 29–30 | JSP + CRUD | `webapp/` (`index.jsp`, `events.jsp`, `editEvent.jsp`, `eventAction.jsp`) |

---

## 6. If something goes wrong

| Message | Fix |
|---|---|
| `No suitable driver found` / `ClassNotFoundException ... mysql` | jar missing from `lib/`. Run `./compile.sh` again afterwards |
| `Access denied for user` | wrong password in `DBConnection.java` |
| `Communications link failure` | MySQL isn't running |
| `Unknown database 'campusserve'` | step 1C not done |
| `permission denied: ./compile.sh` | run `chmod +x compile.sh run.sh` |
| `package ... does not exist` in VS Code | `Cmd+Shift+P` → "Java: Clean Java Language Server Workspace" → Restart |
| `Address already in use` | the server is already running in another terminal |
| "Cannot reach CampusServer" | start `./run.sh campusserve.network.CampusServer` in a second terminal |

## 7. Honest notes
- **Applets:** browsers no longer support them, so `NoticeApplet` (a real `JApplet`) is hosted inside a normal window. Say this in your viva. Use JDK 17 or 21 (the newest JDKs removed the Applet classes).
- **RMI:** the stub is created automatically at run time (Java 5+); skeletons are obsolete (the client `lookup` object is the stub).
- **Passwords:** SHA-256 hashing (plain Java has no bcrypt).
- **Tested by me:** everything compiles on JDK 21; the model/validation/file/thread logic and the socket + RMI server ran correctly. **Not tested by me** (no MySQL, Tomcat or screen here): the database calls, the Swing windows, and the JSP pages. Run the demo script once and send me the exact error text if anything fails.
