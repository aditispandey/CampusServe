package campusserve.ui;

import campusserve.dao.EventDAO;
import campusserve.dao.ShiftDAO;
import campusserve.exceptions.InvalidInputException;
import campusserve.model.Coordinator;
import campusserve.model.ShiftRole;
import campusserve.util.ActivityLog;
import campusserve.util.Validator;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class CoordinatorDashboard extends BaseDashboard {
    private final EventDAO eventDAO = new EventDAO();
    private final ShiftDAO shiftDAO = new ShiftDAO();

    private final DefaultTableModel eventModel = newModel("EventID", "Event", "Start", "End", "Status");
    private final DefaultTableModel shiftModel = newModel("ShiftID", "Event", "Role", "Start", "Slots", "Filled");
    private final DefaultTableModel rosterModel = newModel("AssignmentID", "Volunteer", "Role", "Status");
    private final JTable eventTable = new JTable(eventModel);
    private final JTable shiftTable = new JTable(shiftModel);
    private final JTable rosterTable = new JTable(rosterModel);

    private ArrayList<ShiftRole> shifts = new ArrayList<>();      // shifts currently shown

    public CoordinatorDashboard(Coordinator coordinator) {
        super(coordinator);
        init();
        refresh();
    }

    @Override
    protected JComponent buildContent() {
        // ----- events tab -----
        JButton createEventBtn = new JButton("Create Event");
        createEventBtn.addActionListener(e -> createEvent());
        JButton addShiftBtn = new JButton("Add Shift to Selected Event");
        addShiftBtn.addActionListener(e -> addShift());
        JPanel eventButtons = new JPanel();
        eventButtons.add(createEventBtn);
        eventButtons.add(addShiftBtn);
        JPanel eventPanel = new JPanel(new BorderLayout());
        eventPanel.add(new JScrollPane(eventTable), BorderLayout.CENTER);
        eventPanel.add(eventButtons, BorderLayout.SOUTH);

        // ----- shifts tab -----
        JButton duplicateBtn = new JButton("Duplicate Selected Shift");
        duplicateBtn.addActionListener(e -> duplicateShift());
        JButton deleteBtn = new JButton("Delete Selected Shift");
        deleteBtn.addActionListener(e -> deleteShift());
        JPanel shiftButtons = new JPanel();
        shiftButtons.add(duplicateBtn);
        shiftButtons.add(deleteBtn);
        JPanel shiftPanel = new JPanel(new BorderLayout());
        shiftPanel.add(new JScrollPane(shiftTable), BorderLayout.CENTER);
        shiftPanel.add(shiftButtons, BorderLayout.SOUTH);

        // ----- roster tab -----
        JButton completeBtn = new JButton("Mark Selected Assignment Complete");
        completeBtn.addActionListener(e -> markComplete());
        JPanel rosterPanel = new JPanel(new BorderLayout());
        rosterPanel.add(new JScrollPane(rosterTable), BorderLayout.CENTER);
        rosterPanel.add(completeBtn, BorderLayout.SOUTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("My Events", eventPanel);
        tabs.addTab("Shifts", shiftPanel);
        tabs.addTab("Roster", rosterPanel);

        JButton refreshBtn = new JButton("Refresh");
        refreshBtn.addActionListener(e -> refresh());
        JButton browseBtn = new JButton("Browse Volunteers");
        browseBtn.addActionListener(e -> new VolunteerBrowserDialog(this).setVisible(true));
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(refreshBtn);
        top.add(browseBtn);

        JPanel content = new JPanel(new BorderLayout());
        content.add(top, BorderLayout.NORTH);
        content.add(tabs, BorderLayout.CENTER);
        return content;
    }

    private void refresh() {
        try {
            fill(eventModel, eventDAO.getEventsByCoordinator(user.getUserId()));
            shifts = shiftDAO.getShiftsForCoordinator(user.getUserId());
            List<Object[]> rows = new ArrayList<>();
            for (ShiftRole s : shifts) {
                rows.add(new Object[]{ s.getShiftId(), s.getEventName(), s.getRoleName(), s.getStartTime(), s.getSlotsAvailable(), s.getSlotsFilled() });
            }
            fill(shiftModel, rows);
            fill(rosterModel, shiftDAO.getRoster(user.getUserId()));
        } catch (SQLException ex) {
            Messages.error(this, "Could not load data: " + ex.getMessage());
        }
    }

    private void createEvent() {
        JTextField name = new JTextField();
        JTextField start = new JTextField("2026-11-10");
        JTextField end = new JTextField("2026-11-10");
        Object[] form = { "Event name:", name, "Start date (yyyy-MM-dd):", start, "End date (yyyy-MM-dd):", end };
        if (JOptionPane.showConfirmDialog(this, form, "Create Event", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
        try {
            Validator.require("Event name", name.getText());
            Date startDate = Validator.parseDate("Start date", start.getText());
            Date endDate = Validator.parseDate("End date", end.getText());
            if (endDate.before(startDate)) throw new InvalidInputException("End date cannot be before start date.");

            int clubId = eventDAO.getClubIdForCoordinator(user.getUserId());
            if (clubId == -1) {
                Messages.show(this, "You are not assigned to any club.");
                return;
            }
            eventDAO.createEvent(clubId, name.getText().trim(), startDate, endDate);
            ActivityLog.log(user.getEmail() + " created event " + name.getText().trim());
            Messages.show(this, "Event created. It now waits for admin approval.");
            refresh();
        } catch (InvalidInputException ex) {
            Messages.show(this, ex.getMessage(), JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            Messages.error(this, "Database error: " + ex.getMessage());
        }
    }

    private void addShift() {
        int eventId = selectedId(eventTable, eventModel);
        if (eventId == -1) return;

        JTextField role = new JTextField();
        JTextField start = new JTextField("2026-11-10 09:00");
        JTextField end = new JTextField("2026-11-10 13:00");
        JTextField slots = new JTextField("5");
        Object[] form = { "Role name:", role, "Start (yyyy-MM-dd HH:mm):", start, "End (yyyy-MM-dd HH:mm):", end, "Slots:", slots };
        if (JOptionPane.showConfirmDialog(this, form, "Add Shift", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;

        try {                                                     // OUTER try
            try {                                                 // NESTED (inner) try
                Validator.require("Role name", role.getText());
                Timestamp s = Validator.parseDateTime("Start", start.getText());
                Timestamp e = Validator.parseDateTime("End", end.getText());
                int slotCount = Validator.parseInt("Slots", slots.getText());
                Validator.require("Slots", slotCount);                   // overloaded require(String,int)
                if (!e.after(s)) throw new InvalidInputException("End time must be after start time.");

                ShiftRole shift = new ShiftRole(0, eventId, "", role.getText().trim(), s, e, slotCount, 0);   // parameterized constructor
                shiftDAO.createShiftRole(shift);
            } catch (InvalidInputException ex) {
                Messages.show(this, ex.getMessage(), JOptionPane.WARNING_MESSAGE);
                return;
            }
            ActivityLog.log(user.getEmail() + " added shift " + role.getText().trim() + " to event " + eventId);
            Messages.show(this, "Shift added.");
            refresh();
        } catch (SQLException ex) {                               // OUTER catch
            Messages.error(this, "Database error: " + ex.getMessage());
        }
    }

    private void duplicateShift() {
        int shiftId = selectedId(shiftTable, shiftModel);
        if (shiftId == -1) return;
        try {
            for (ShiftRole original : shifts) {
                if (original.getShiftId() == shiftId) {
                    ShiftRole copy = new ShiftRole(original);              // COPY constructor
                    copy.setRoleName("Copy of " + original.getRoleName());
                    copy.setSlotsFilled(0);
                    shiftDAO.createShiftRole(copy);
                    ActivityLog.log(user.getEmail() + " duplicated shift " + shiftId);
                    Messages.show(this, "Shift duplicated.");
                    refresh();
                    return;
                }
            }
        } catch (SQLException ex) {
            Messages.error(this, "Database error: " + ex.getMessage());
        }
    }

    private void deleteShift() {
        int shiftId = selectedId(shiftTable, shiftModel);
        if (shiftId == -1) return;
        if (JOptionPane.showConfirmDialog(this, "Delete shift " + shiftId + "?", "Confirm", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try {
            shiftDAO.deleteShift(shiftId);
            ActivityLog.log(user.getEmail() + " deleted shift " + shiftId);
            refresh();
        } catch (SQLException ex) {
            Messages.error(this, "Cannot delete (volunteers may already be registered):\n" + ex.getMessage());
        }
    }

    private void markComplete() {
        int id = selectedId(rosterTable, rosterModel);
        if (id == -1) return;
        try {
            shiftDAO.markComplete(id);
            ActivityLog.log(user.getEmail() + " completed assignment " + id);
            refresh();
        } catch (SQLException ex) {
            Messages.error(this, "Database error: " + ex.getMessage());
        }
    }
}
