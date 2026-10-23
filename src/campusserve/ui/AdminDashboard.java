package campusserve.ui;

import campusserve.dao.EventDAO;
import campusserve.exceptions.InvalidInputException;
import campusserve.model.Administrator;
import campusserve.network.CertificateClient;
import campusserve.util.ActivityLog;
import java.awt.*;
import java.io.IOException;
import java.sql.SQLException;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class AdminDashboard extends BaseDashboard {
    private final EventDAO eventDAO = new EventDAO();
    private final DefaultTableModel model = newModel("EventID", "Event", "Club", "Start", "Status");
    private final JTable table = new JTable(model);
    private final JTextField searchField = new JTextField(14);

    public AdminDashboard(Administrator admin) {
        super(admin);
        init();
        refresh();
    }

    @Override
    protected JComponent buildContent() {
        JButton approveBtn = new JButton("Approve");
        approveBtn.addActionListener(e -> decide("APPROVED"));
        JButton rejectBtn = new JButton("Reject");
        rejectBtn.addActionListener(e -> decide("REJECTED"));
        JButton deleteBtn = new JButton("Delete (pending only)");
        deleteBtn.addActionListener(e -> deleteEvent());
        JButton verifyBtn = new JButton("Verify Certificate (RMI)");
        verifyBtn.addActionListener(e -> verifyCertificate());
        JButton logBtn = new JButton("View Activity Log (file)");
        logBtn.addActionListener(e -> viewLog());
        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> refresh());

        JPanel buttons = new JPanel();
        buttons.add(approveBtn);
        buttons.add(rejectBtn);
        buttons.add(deleteBtn);
        buttons.add(verifyBtn);
        buttons.add(logBtn);
        buttons.add(refreshBtn);

        // search bar
        JButton searchBtn = new JButton("Search");
        searchBtn.addActionListener(e -> refresh());
        JButton clearBtn = new JButton("Clear");
        clearBtn.addActionListener(e -> { searchField.setText(""); refresh(); });
        searchField.addActionListener(e -> refresh());                  // Enter key also searches
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchBar.add(new JLabel("Search event, club or status:"));
        searchBar.add(searchField);
        searchBar.add(searchBtn);
        searchBar.add(clearBtn);

        JPanel content = new JPanel(new BorderLayout());
        content.add(searchBar, BorderLayout.NORTH);
        content.add(new JScrollPane(table), BorderLayout.CENTER);
        content.add(buttons, BorderLayout.SOUTH);
        return content;
    }

    private void refresh() {
        try {
            fill(model, eventDAO.searchEvents(searchField.getText()));
        } catch (SQLException ex) {
            Messages.error(this, "Could not load events: " + ex.getMessage());
        }
    }

    private void decide(String status) {
        int id = selectedId(table, model);
        if (id == -1) return;
        try {
            eventDAO.updateApprovalStatus(id, status);
            ActivityLog.log(user.getEmail() + " set event " + id + " to " + status);
            refresh();
        } catch (SQLException ex) {
            Messages.error(this, "Database error: " + ex.getMessage());
        }
    }

    private void deleteEvent() {
        int id = selectedId(table, model);
        if (id == -1) return;
        try {
            eventDAO.deletePendingEvent(id);
            ActivityLog.log(user.getEmail() + " deleted event " + id);
            refresh();
        } catch (InvalidInputException ex) {
            Messages.show(this, ex.getMessage(), JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            Messages.error(this, "Database error: " + ex.getMessage());
        }
    }

    private void verifyCertificate() {
        String code = JOptionPane.showInputDialog(this, "Certificate code (e.g. CS-1-...):");
        if (code == null || code.trim().isEmpty()) return;
        try {
            Messages.show(this, CertificateClient.verify(code));
        } catch (Exception ex) {
            Messages.error(this, "Cannot reach CampusServer. Start it first (see guide).\n" + ex.getMessage());
        }
    }

    private void viewLog() {
        try {
            JTextArea area = new JTextArea(ActivityLog.readAll(), 18, 70);
            area.setEditable(false);
            JOptionPane.showMessageDialog(this, new JScrollPane(area), "Activity Log", JOptionPane.PLAIN_MESSAGE);
        } catch (IOException ex) {
            Messages.error(this, "Could not read log: " + ex.getMessage());
        }
    }
}
