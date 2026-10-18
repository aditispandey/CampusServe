package campusserve.dao;

import campusserve.db.DBConnection;
import campusserve.model.*;
import campusserve.util.PasswordUtil;
import java.sql.*;

public class UserDAO {

    /** Returns the correct User child (Volunteer / Coordinator / Administrator) or null if login is wrong. */
    public User login(String email, String password) throws SQLException {
        String sql = "SELECT u.UserID, u.Name, u.PasswordHash, u.Role, vp.VolunteerID, vp.TotalHoursServed " +
                     "FROM User u LEFT JOIN VolunteerProfile vp ON vp.UserID = u.UserID WHERE u.Email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                if (!rs.getString("PasswordHash").equals(PasswordUtil.hash(password))) return null;

                int userId = rs.getInt("UserID");
                String name = rs.getString("Name");
                switch (rs.getString("Role")) {
                    case "VOLUNTEER":
                        return new Volunteer(userId, name, email, rs.getInt("VolunteerID"), rs.getDouble("TotalHoursServed"));
                    case "COORDINATOR":
                        return new Coordinator(userId, name, email);
                    default:
                        return new Administrator(userId, name, email);
                }
            }
        }
    }

    /** Two INSERTs in one transaction: the User row and its VolunteerProfile row. */
    public void registerVolunteer(String name, String email, String password) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int userId;
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO User (Name, Email, PasswordHash, Role) VALUES (?, ?, ?, 'VOLUNTEER')",
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, name);
                    ps.setString(2, email);
                    ps.setString(3, PasswordUtil.hash(password));
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        keys.next();
                        userId = keys.getInt(1);
                    }
                }
                try (PreparedStatement ps = conn.prepareStatement("INSERT INTO VolunteerProfile (UserID) VALUES (?)")) {
                    ps.setInt(1, userId);
                    ps.executeUpdate();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }
}
