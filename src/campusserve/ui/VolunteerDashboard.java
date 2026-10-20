package campusserve.ui;

import campusserve.dao.ShiftDAO;
import campusserve.exceptions.DoubleBookingException;
import campusserve.exceptions.InsufficientSlotsException;
import campusserve.model.ShiftRole;
import campusserve.model.Volunteer;
import campusserve.network.SlotClient;
import campusserve.tasks.ExportTask;
import campusserve.tasks.SlotMonitor;
import campusserve.util.ActivityLog;
import campusserve.util.Rules;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class VolunteerDashboard extends BaseDashboard {
    private final Volunteer volunteer;
    private final ShiftDAO shiftDAO = new ShiftDAO();

    private final DefaultTableModel openModel = newModel("ShiftID", "Event", "Role", "Start", "Slots Open");
    private final DefaultTableModel myModel = newModel("AssignmentID", "Role", "Start", "Status", "Hours");
    private final JTable openTable = new JTable(openModel);
    private final JTable myTable = new JTable(myModel);
    private final JLabel hoursLabel = new JLabel(" ");
    private final JLabel liveLabel = new JLabel(" ");
    private final JTextField searchField = new JTextField(14);
    private SlotMonitor monitor;

    public VolunteerDashboard(Volunteer volunteer) {
        super(volunteer);                 // calls BaseDashboard's constructor
        this.volunteer = volunteer;
        init();                           // builds the screen via buildContent()
        refresh();
        startMonitor();
    }

    @Override
    protected JComponent buildContent() {
        // ----- tab 1: open shifts -----
        JButton registerBtn = new JButton("Register for Selected Shift");
        registerBtn.addActionListener(new RegisterAction());              // INNER class
        JButton checkBtn = new JButton("Check Slots on Server (socket)");
        checkBtn.addActionListener(e -> checkSlotsOnServer());
        JPanel openButtons = new JPanel();
        openButtons.add(registerBtn);
        openButtons.add(checkBtn);

        // search bar for the open shifts
        JButton searchBtn = new JButton("Search");
        searchBtn.addActionListener(e -> refresh());
        JButton clearBtn = new JButton("Clear");
        clearBtn.addActionListener(e -> { searchField.setText(""); refresh(); });
        searchField.addActionListener(e -> refresh());                  // Enter key also searches
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchBar.add(new JLabel("Search role or event:"));
        searchBar.add(searchField);
        searchBar.add(searchBtn);
        searchBar.add(clearBtn);

        JPanel openPanel = new JPanel(new BorderLayout());
        openPanel.add(searchBar, BorderLayout.NORTH);
        openPanel.add(new JScrollPane(openTable), BorderLayout.CENTER);
        openPanel.add(openButtons, BorderLayout.SOUTH);

        // ----- tab 2: my shifts -----
        JButton cancelBtn = new JButton("Cancel Selected Shift");
        cancelBtn.addActionListener(new ActionListener() {               // ANONYMOUS class
            @Override
            public void actionPerformed(ActionEvent e) { cancelSelected(); }
        });
        JButton exportBtn = new JButton("Export My Report (file)");
        exportBtn.addActionListener(e -> new Thread(new ExportTask(volunteer, this), "ExportThread").start());
        JPanel myButtons = new JPanel();
        myButtons.add(cancelBtn);
        myButtons.add(exportBtn);
        JPanel myPanel = new JPanel(new BorderLayout());
        myPanel.add(new JScrollPane(myTable), BorderLayout.CENTER);
        myPanel.add(myButtons, BorderLayout.SOUTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Open Shifts", openPanel);
        tabs.addTab("My Shifts", myPanel);

        // ----- top info bar -----
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> refresh());
        JButton threadBtn = new JButton("Monitor Thread Status");
        threadBtn.addActionListener(e -> Messages.show(this,
            "SlotMonitor thread\nState: " + monitor.getState() + "\nAlive: " + monitor.isAlive()));
        JPanel buttons = new JPanel();
        buttons.add(refreshBtn);
        buttons.add(threadBtn);

        JPanel info = new JPanel(new GridLayout(2, 1));
        info.add(hoursLabel);
        info.add(liveLabel);
        JPanel top = new JPanel(new BorderLayout());
        top.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        top.add(info, BorderLayout.CENTER);
        top.add(buttons, BorderLayout.EAST);

        JPanel content = new JPanel(new BorderLayout());
        content.add(top, BorderLayout.NORTH);
        content.add(tabs, BorderLayout.CENTER);
        return content;
    }

    // ---------------- data loading ----------------

    private void refresh() {
        try {
            ArrayList<ShiftRole> all = shiftDAO.getOpenShifts();                           // every open shift (for the live label)
            ArrayList<ShiftRole> open = shiftDAO.searchOpenShifts(searchField.getText());  // shifts matching the search box

            // ANONYMOUS Comparator: shifts with the most open slots first
            Collections.sort(open, new Comparator<ShiftRole>() {
                @Override
                public int compare(ShiftRole a, ShiftRole b) {
                    return Integer.compare(b.getOpenSlots(), a.getOpenSlots());
                }
            });

            List<Object[]> rows = new ArrayList<>();
            for (ShiftRole s : open) {
                rows.add(new Object[]{ s.getShiftId(), s.getEventName(), s.getRoleName(), s.getStartTime(), s.getOpenSlots() });
            }
            fill(openModel, rows);
            fill(myModel, shiftDAO.getMyShifts(volunteer.getVolunteerId()));

            Double hours = shiftDAO.getTotalHours(volunteer.getVolunteerId());     // wrapper class
            List<String> certs = shiftDAO.getCertificateCodes(volunteer.getVolunteerId());
            hoursLabel.setText("Total hours: " + Rules.formatHours(hours)
                + "   |   Hours to next certificate: " + Rules.hoursToCertificate(hours)
                + "   |   Certificates: " + (certs.isEmpty() ? "none yet" : certs));
            liveLabel.setText("Live: " + countOpenSlots(all) + " open slots in " + all.size() + " shifts");
        } catch (SQLException ex) {
            Messages.error(this, "Could not load data: " + ex.getMessage());
        }
    }

    private int countOpenSlots(List<ShiftRole> shifts) {
        int total = 0;
        for (ShiftRole s : shifts) total += s.getOpenSlots();
        return total;
    }

    // ---------------- background thread ----------------

    private void startMonitor() {
        monitor = new SlotMonitor(10000, new Runnable() {              // runs every 10 seconds, on the monitor thread
            @Override
            public void run() {
                try {
                    final ArrayList<ShiftRole> open = shiftDAO.getOpenShifts();
                    SwingUtilities.invokeLater(() ->
                        liveLabel.setText("Live: " + countOpenSlots(open) + " open slots in " + open.size()
                            + " shifts (auto-updated every 10 s)"));
                } catch (SQLException e) {
                    SwingUtilities.invokeLater(() -> liveLabel.setText("Live update failed: " + e.getMessage()));
                }
            }
        });
        ActivityLog.log("SlotMonitor state before start: " + monitor.getState());    // NEW
        monitor.start();
        ActivityLog.log("SlotMonitor state after start: " + monitor.getState());     // RUNNABLE
    }

    @Override
    public void dispose() {
        if (monitor != null) monitor.stopMonitor();
        super.dispose();
    }

    // ---------------- actions ----------------

    /** INNER class listener for the Register button. */
    private class RegisterAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            int shiftId = selectedId(openTable, openModel);
            if (shiftId == -1) return;
            try {
                // find the chosen shift to show what the hours would become
                ShiftRole chosen = null;
                for (ShiftRole s : shiftDAO.getOpenShifts()) if (s.getShiftId() == shiftId) chosen = s;
                if (chosen != null) {
                    Volunteer preview = new Volunteer(volunteer);                 // COPY constructor
                    long minutes = chosen.getMinutes();
                    preview.addHours((int) (minutes / 60), (int) (minutes % 60)); // overloaded addHours(int,int)
                    double current = shiftDAO.getTotalHours(volunteer.getVolunteerId());
                    preview.addHours(current);                                    // overloaded addHours(double)
                    int ok = JOptionPane.showConfirmDialog(VolunteerDashboard.this,
                        "Register for " + chosen.getRoleName() + " (" + Rules.formatHours((int) (minutes / 60), (int) (minutes % 60)) + ")?\n"
                        + "Hours after you complete it: " + preview.getTotalHoursServed(),
                        "Confirm", JOptionPane.YES_NO_OPTION);
                    if (ok != JOptionPane.YES_OPTION) return;
                }
                shiftDAO.registerForShift(volunteer.getVolunteerId(), shiftId);
                ActivityLog.log(volunteer.getEmail() + " registered for shift " + shiftId);
                Messages.show(VolunteerDashboard.this, "Registered successfully!");
                refresh();
            } catch (DoubleBookingException | InsufficientSlotsException ex) {   // multiple catch
                Messages.show(VolunteerDashboard.this, ex.getMessage(), JOptionPane.WARNING_MESSAGE);
            } catch (SQLException ex) {
                Messages.error(VolunteerDashboard.this, "Database error: " + ex.getMessage());
            }
        }
    }

    private void cancelSelected() {
        int id = selectedId(myTable, myModel);
        if (id == -1) return;
        try {
            shiftDAO.cancelAssignment(id);
            ActivityLog.log(volunteer.getEmail() + " cancelled assignment " + id);
            refresh();
        } catch (SQLException ex) {
            Messages.error(this, "Database error: " + ex.getMessage());
        }
    }

    private void checkSlotsOnServer() {
        int shiftId = selectedId(openTable, openModel);
        if (shiftId == -1) return;
        try {
            Messages.show(this, "Server says: " + SlotClient.ask("SLOTS " + shiftId));
        } catch (IOException ex) {
            Messages.error(this, "Cannot reach CampusServer. Start it first (see guide).\n" + ex.getMessage());
        }
    }
}
