package campusserve.network;

import campusserve.db.DBConnection;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.*;

/** RMI REMOTE OBJECT: lives on the server and answers certificate checks from the database. */
public class CertificateServiceImpl extends UnicastRemoteObject implements CertificateService {

    public CertificateServiceImpl() throws RemoteException { super(); }

    @Override
    public String verify(String certificateCode) throws RemoteException {
        String sql = "SELECT u.Name, c.IssueDate, c.HoursThreshold FROM Certificate c " +
                     "JOIN VolunteerProfile vp ON vp.VolunteerID = c.VolunteerID " +
                     "JOIN User u ON u.UserID = vp.UserID WHERE c.CertificateCode = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, certificateCode.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return "VALID - issued to " + rs.getString(1) + " on " + rs.getDate(2)
                         + " for " + rs.getInt(3) + "+ volunteer hours.";
                }
                return "NOT FOUND - no certificate with code " + certificateCode;
            }
        } catch (SQLException e) {
            return "Server database error: " + e.getMessage();
        }
    }
}
