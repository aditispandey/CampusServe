package campusserve;

import campusserve.db.DBConnection;
import campusserve.ui.LoginFrame;

import javax.swing.SwingUtilities;
import java.sql.Connection;
import java.sql.SQLException;

/** Start here. Runs from the command line: checks MySQL, then opens the login window. */
public class Main {

    // variables
    static final String APP_NAME = "CampusServe";
    static final String VERSION = "1.0";

    // methods
    static void printBanner() {
        System.out.println("==========================================");
        System.out.println("  " + APP_NAME + " v" + VERSION + " - Volunteer & Shift Network");
        System.out.println("==========================================");
    }

    static boolean databaseReachable() {
        try (Connection conn = DBConnection.getConnection()) {
            return conn.isValid(2);
        } catch (SQLException e) {
            System.out.println("Database problem: " + e.getMessage());
            return false;
        }
    }

    public static void main(String[] args) {
        printBanner();
        boolean ok = databaseReachable();
        System.out.println("Database connection: " + (ok ? "OK" : "FAILED"));
        if (!ok) {
            System.out.println("Check: MySQL running? Password correct in DBConnection.java? Jar in lib/ ?");
            return;
        }
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
