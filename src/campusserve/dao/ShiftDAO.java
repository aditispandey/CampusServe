package campusserve.dao;

import campusserve.db.DBConnection;
import campusserve.exceptions.DoubleBookingException;
import campusserve.exceptions.InsufficientSlotsException;
import campusserve.model.ShiftRole;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ShiftDAO {

    private static final String SHIFT_SELECT =
        "SELECT s.ShiftID, s.EventID, e.EventName, s.RoleName, s.StartTime, s.EndTime, s.SlotsAvailable, s.SlotsFilled " +
        "FROM ShiftRole s JOIN Event e ON e.EventID = s.EventID ";

    /** Turns the current ResultSet row into a ShiftRole (default constructor + setters). */
    private ShiftRole mapShift(ResultSet rs) throws SQLException {
        ShiftRole s = new ShiftRole();
        s.setShiftId(rs.getInt("ShiftID"));
        s.setEventId(rs.getInt("EventID"));
        s.setEventName(rs.getString("EventName"));
        s.setRoleName(rs.getString("RoleName"));
        s.setStartTime(rs.getTimestamp("StartTime"));
        s.setEndTime(rs.getTimestamp("EndTime"));
        s.setSlotsAvailable(rs.getInt("SlotsAvailable"));
        s.setSlotsFilled(rs.getInt("SlotsFilled"));
        return s;
    }

    /** Open shifts of APPROVED events. Plain Statement (no parameters). */
    public ArrayList<ShiftRole> getOpenShifts() throws SQLException {
        String sql = SHIFT_SELECT + "WHERE e.ApprovalStatus = 'APPROVED' AND s.SlotsAvailable > s.SlotsFilled ORDER BY s.StartTime";
        ArrayList<ShiftRole> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapShift(rs));
        }
        return list;
    }

    public ArrayList<ShiftRole> getShiftsForCoordinator(int coordinatorUserId) throws SQLException {
        String sql = SHIFT_SELECT + "JOIN Club c ON c.ClubID = e.ClubID WHERE c.CoordinatorUserID = ? ORDER BY s.ShiftID";
        ArrayList<ShiftRole> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, coordinatorUserId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapShift(rs));
            }
        }
        return list;
    }

    /** Free slots of one shift, or -1 if the shift does not exist (used by the socket server). */
    public int getOpenSlots(int shiftId) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT SlotsAvailable - SlotsFilled FROM ShiftRole WHERE ShiftID = ?")) {
            ps.setInt(1, shiftId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : -1;
            }
        }
    }

    public List<Object[]> getMyShifts(int volunteerId) throws SQLException {
        String sql = "SELECT sa.AssignmentID, s.RoleName, s.StartTime, sa.Status, sa.HoursLogged " +
                     "FROM ShiftAssignment sa JOIN ShiftRole s ON s.ShiftID = sa.ShiftID WHERE sa.VolunteerUserID = ? ORDER BY s.StartTime";
        List<Object[]> rows = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, volunteerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new Object[]{ rs.getInt(1), rs.getString(2), rs.getTimestamp(3), rs.getString(4), rs.getDouble(5) });
                }
            }
        }
        return rows;
    }

    /**
     * Register for a shift - a TRANSACTION: the assignment row and the slot count change together, or not at all.
     * 'throws' lists the problems the caller must handle.
     */
    public void registerForShift(int volunteerId, int shiftId)
            throws SQLException, DoubleBookingException, InsufficientSlotsException {

        Connection conn = DBConnection.getConnection();
        try {
            conn.setAutoCommit(false);

            // 1. lock the shift row and read its slots / times
            int available, filled;
            Timestamp start, end;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT SlotsAvailable, SlotsFilled, StartTime, EndTime FROM ShiftRole WHERE ShiftID = ? FOR UPDATE")) {
                ps.setInt(1, shiftId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) throw new SQLException("Shift not found: " + shiftId);
                    available = rs.getInt(1);
                    filled = rs.getInt(2);
                    start = rs.getTimestamp(3);
                    end = rs.getTimestamp(4);
                }
            }
            if (filled >= available) {
                throw new InsufficientSlotsException("Sorry, this shift is full.");
            }

            // 2. overlapping shift?
            String overlap = "SELECT COUNT(*) FROM ShiftAssignment sa JOIN ShiftRole sr ON sr.ShiftID = sa.ShiftID " +
                             "WHERE sa.VolunteerUserID = ? AND sa.Status IN ('REGISTERED','COMPLETED') " +
                             "AND sr.StartTime < ? AND sr.EndTime > ?";
            try (PreparedStatement ps = conn.prepareStatement(overlap)) {
                ps.setInt(1, volunteerId);
                ps.setTimestamp(2, end);
                ps.setTimestamp(3, start);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    if (rs.getInt(1) > 0) {
                        throw new DoubleBookingException("You already have a shift that overlaps with this one.");
                    }
                }
            }

            // 3. insert assignment, 4. increase slots filled
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO ShiftAssignment (ShiftID, VolunteerUserID, Status) VALUES (?, ?, 'REGISTERED')")) {
                ps.setInt(1, shiftId);
                ps.setInt(2, volunteerId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE ShiftRole SET SlotsFilled = SlotsFilled + 1 WHERE ShiftID = ?")) {
                ps.setInt(1, shiftId);
                ps.executeUpdate();
            }
            conn.commit();

        } catch (SQLException | DoubleBookingException | InsufficientSlotsException e) {
            conn.rollback();           // undo everything
            throw e;                   // 'throw' the same problem on to the caller
        } finally {
            conn.close();              // always runs, success or failure
        }
    }

    /** Cancel: only changes Status. A DATABASE TRIGGER gives the slot back. */
    public void cancelAssignment(int assignmentId) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "UPDATE ShiftAssignment SET Status = 'CANCELLED' WHERE AssignmentID = ? AND Status = 'REGISTERED'")) {
            ps.setInt(1, assignmentId);
            ps.executeUpdate();
        }
    }

    // ---------- coordinator side ----------

    public void createShiftRole(ShiftRole s) throws SQLException {
        String sql = "INSERT INTO ShiftRole (EventID, RoleName, StartTime, EndTime, SlotsAvailable) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, s.getEventId());
            ps.setString(2, s.getRoleName());
            ps.setTimestamp(3, s.getStartTime());
            ps.setTimestamp(4, s.getEndTime());
            ps.setInt(5, s.getSlotsAvailable());
            ps.executeUpdate();
        }
    }

    /** DELETE. MySQL refuses (SQLException) if volunteers are already registered. */
    public void deleteShift(int shiftId) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM ShiftRole WHERE ShiftID = ?")) {
            ps.setInt(1, shiftId);
            ps.executeUpdate();
        }
    }

    public List<Object[]> getRoster(int coordinatorUserId) throws SQLException {
        String sql = "SELECT sa.AssignmentID, u.Name, s.RoleName, sa.Status " +
                     "FROM ShiftAssignment sa " +
                     "JOIN ShiftRole s ON s.ShiftID = sa.ShiftID " +
                     "JOIN Event e ON e.EventID = s.EventID " +
                     "JOIN Club c ON c.ClubID = e.ClubID " +
                     "JOIN VolunteerProfile vp ON vp.UserID = sa.VolunteerUserID " +
                     "JOIN User u ON u.UserID = vp.UserID WHERE c.CoordinatorUserID = ? ORDER BY sa.AssignmentID";
        List<Object[]> rows = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, coordinatorUserId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new Object[]{ rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4) });
                }
            }
        }
        return rows;
    }

    /** Mark COMPLETED and log the shift length as hours. A TRIGGER then adds the hours and may issue a certificate. */
    public void markComplete(int assignmentId) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            double hours = 0;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT TIMESTAMPDIFF(MINUTE, s.StartTime, s.EndTime) / 60.0 " +
                    "FROM ShiftAssignment sa JOIN ShiftRole s ON s.ShiftID = sa.ShiftID WHERE sa.AssignmentID = ?")) {
                ps.setInt(1, assignmentId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) hours = rs.getDouble(1);
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE ShiftAssignment SET Status = 'COMPLETED', HoursLogged = ? WHERE AssignmentID = ? AND Status = 'REGISTERED'")) {
                ps.setDouble(1, hours);
                ps.setInt(2, assignmentId);
                ps.executeUpdate();
            }
        }
    }

    // ---------- hours and certificates ----------

    public double getTotalHours(int volunteerId) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT TotalHoursServed FROM VolunteerProfile WHERE UserID = ?")) {
            ps.setInt(1, volunteerId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : 0;
            }
        }
    }

    public List<String> getCertificateCodes(int volunteerId) throws SQLException {
        List<String> codes = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT CertificateCode FROM Certificate WHERE VolunteerUserID = ?")) {
            ps.setInt(1, volunteerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) codes.add(rs.getString(1));
            }
        }
        return codes;
    }
}
