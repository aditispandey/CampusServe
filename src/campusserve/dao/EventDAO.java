package campusserve.dao;

import campusserve.db.DBConnection;
import campusserve.exceptions.InvalidInputException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EventDAO {

    public int getClubIdForCoordinator(int userId) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT ClubID FROM Club WHERE CoordinatorUserID = ? LIMIT 1")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : -1;
            }
        }
    }

    // INSERT
    public void createEvent(int clubId, String name, Date start, Date end) throws SQLException {
        String sql = "INSERT INTO Event (EventName, ClubID, StartDate, EndDate) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setInt(2, clubId);
            ps.setDate(3, start);
            ps.setDate(4, end);
            ps.executeUpdate();
        }
    }

    // SELECT with PreparedStatement (has a ? parameter)
    public List<Object[]> getEventsByCoordinator(int userId) throws SQLException {
        String sql = "SELECT e.EventID, e.EventName, e.StartDate, e.EndDate, e.ApprovalStatus " +
                     "FROM Event e JOIN Club c ON c.ClubID = e.ClubID WHERE c.CoordinatorUserID = ? ORDER BY e.EventID";
        List<Object[]> rows = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new Object[]{ rs.getInt(1), rs.getString(2), rs.getDate(3), rs.getDate(4), rs.getString(5) });
                }
            }
        }
        return rows;
    }

    // SELECT with plain Statement (no parameters needed)
    public List<Object[]> getAllEvents() throws SQLException {
        String sql = "SELECT e.EventID, e.EventName, c.ClubName, e.StartDate, e.ApprovalStatus " +
                     "FROM Event e JOIN Club c ON c.ClubID = e.ClubID ORDER BY e.EventID";
        List<Object[]> rows = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                rows.add(new Object[]{ rs.getInt(1), rs.getString(2), rs.getString(3), rs.getDate(4), rs.getString(5) });
            }
        }
        return rows;
    }

    // SEARCH: events whose name, club name or status contains the keyword
    public List<Object[]> searchEvents(String keyword) throws SQLException {
        String sql = "SELECT e.EventID, e.EventName, c.ClubName, e.StartDate, e.ApprovalStatus " +
                     "FROM Event e JOIN Club c ON c.ClubID = e.ClubID " +
                     "WHERE e.EventName LIKE ? OR c.ClubName LIKE ? OR e.ApprovalStatus LIKE ? ORDER BY e.EventID";
        String like = "%" + keyword.trim() + "%";
        List<Object[]> rows = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new Object[]{ rs.getInt(1), rs.getString(2), rs.getString(3), rs.getDate(4), rs.getString(5) });
                }
            }
        }
        return rows;
    }

    // UPDATE
    public void updateApprovalStatus(int eventId, String status) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE Event SET ApprovalStatus = ? WHERE EventID = ?")) {
            ps.setString(1, status);
            ps.setInt(2, eventId);
            ps.executeUpdate();
        }
    }

    // DELETE (only PENDING events may be removed)
    public void deletePendingEvent(int eventId) throws SQLException, InvalidInputException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM Event WHERE EventID = ? AND ApprovalStatus = 'PENDING'")) {
            ps.setInt(1, eventId);
            if (ps.executeUpdate() == 0) {
                throw new InvalidInputException("Only PENDING events can be deleted.");
            }
        }
    }
}
