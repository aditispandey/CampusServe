package campusserve.dao;

import campusserve.db.DBConnection;
import java.sql.*;

/**
 * Walks through volunteers one record at a time: first / next / previous / last.
 * Uses a SCROLLABLE ResultSet, so the connection stays open until close() is called.
 */
public class VolunteerCursor implements AutoCloseable {
    private final Connection conn;
    private final Statement st;
    private final ResultSet rs;
    private int total;

    public VolunteerCursor() throws SQLException {
        conn = DBConnection.getConnection();
        st = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        rs = st.executeQuery(
            "SELECT u.Name, u.Email, d.DeptName, vp.TotalHoursServed " +
            "FROM VolunteerProfile vp JOIN User u ON u.UserID = vp.UserID " +
            "LEFT JOIN Department d ON d.DeptID = u.DeptID ORDER BY u.Name");
        rs.last();
        total = rs.getRow();          // row number of the last row = number of rows
        rs.beforeFirst();
    }

    public int getTotal() { return total; }
    public int getPosition() throws SQLException { return rs.getRow(); }

    public boolean first() throws SQLException { return rs.first(); }
    public boolean next() throws SQLException { return rs.next(); }
    public boolean previous() throws SQLException { return rs.previous(); }
    public boolean last() throws SQLException { return rs.last(); }

    public String getName() throws SQLException { return rs.getString("Name"); }
    public String getEmail() throws SQLException { return rs.getString("Email"); }
    public String getDept() throws SQLException {
        String d = rs.getString("DeptName");
        return d == null ? "(none)" : d;
    }
    public double getHours() throws SQLException { return rs.getDouble("TotalHoursServed"); }

    @Override
    public void close() {
        try { rs.close(); st.close(); conn.close(); }
        catch (SQLException e) { System.out.println("Error closing cursor: " + e.getMessage()); }
    }
}
