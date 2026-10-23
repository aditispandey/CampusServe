package campusserve.tasks;

import campusserve.dao.ShiftDAO;
import campusserve.model.Volunteer;
import campusserve.ui.Messages;
import campusserve.util.ActivityLog;
import campusserve.util.ReportBuilder;

import javax.swing.SwingUtilities;
import java.awt.Component;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * RUNNABLE: exports the volunteer's report to a text file on a background thread,
 * so the window never freezes while the database and file are busy.
 */
public class ExportTask implements Runnable {
    private final Volunteer volunteer;
    private final Component parent;

    public ExportTask(Volunteer volunteer, Component parent) {
        this.volunteer = volunteer;
        this.parent = parent;
    }

    @Override
    public void run() {
        String message;
        try {
            ShiftDAO dao = new ShiftDAO();
            int id = volunteer.getVolunteerId();
            double hours = dao.getTotalHours(id);
            List<Object[]> shifts = dao.getMyShifts(id);
            List<String> certs = dao.getCertificateCodes(id);

            String text = ReportBuilder.buildVolunteerReport(volunteer, hours, shifts, certs);

            File folder = new File("reports");
            folder.mkdirs();
            File out = new File(folder, volunteer.getName().replace(' ', '_') + "_report.txt");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(out))) {      // FILE WRITE
                writer.write(text);
            }
            message = "Report saved to:\n" + out.getAbsolutePath();
            ActivityLog.log("Report exported for " + volunteer.getName());
        } catch (SQLException | IOException e) {                                          // multiple catch
            message = "Export failed: " + e.getMessage();
        } finally {
            ActivityLog.log("ExportTask finished on thread: " + Thread.currentThread().getName());
        }

        final String shown = message;
        SwingUtilities.invokeLater(() -> Messages.show(parent, shown));    // back to the Swing thread
    }
}
