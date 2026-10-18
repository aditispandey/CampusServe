package campusserve.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** 'final' class (cannot be extended). The ONE place that connects to MySQL. EDIT USER and PASSWORD. */
public final class DBConnection {
    private static final String URL =
        "jdbc:mysql://localhost:3306/campusserve?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER = "root";        // <-- your MySQL username
    private static final String PASSWORD = "your_password_here";    // <-- your MySQL password

    private DBConnection() { }

    /** DriverManager hands back a new Connection each call; callers close it. */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
