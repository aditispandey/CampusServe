package campusserve.ui;

import campusserve.model.User;
import campusserve.util.ActivityLog;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Vector;

/**
 * ABSTRACT class: the common window for all three dashboards (extends JFrame = inheritance).
 * Each child must supply its own buildContent().
 */
public abstract class BaseDashboard extends JFrame {
    protected final User user;

    protected BaseDashboard(User user) {
        super(user.getDashboardTitle());                  // super(...) calls JFrame's constructor
        this.user = user;
        setSize(850, 500);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JButton logoutButton = new JButton("Log out");
        logoutButton.addActionListener(new ActionListener() {          // ANONYMOUS class
            @Override
            public void actionPerformed(ActionEvent e) {
                ActivityLog.log("LOGOUT " + user.getEmail());
                dispose();
                new LoginFrame().setVisible(true);
            }
        });
        JPanel top = new JPanel(new BorderLayout());
        top.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        top.add(new JLabel("Logged in as: " + user.getName() + "  (" + user.getRole() + ")"), BorderLayout.CENTER);
        top.add(logoutButton, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);
    }

    /** Abstract method: each dashboard builds its own screen. */
    protected abstract JComponent buildContent();

    /** 'final' method: children can use it but may not change it. Call it at the end of the child's constructor. */
    protected final void init() {
        add(buildContent(), BorderLayout.CENTER);
    }

    /** A table model whose cells cannot be edited (ANONYMOUS subclass of DefaultTableModel). */
    protected DefaultTableModel newModel(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
    }

    /** Fills a table. Each row goes in as a Vector. */
    protected void fill(DefaultTableModel model, List<Object[]> rows) {
        model.setRowCount(0);
        for (Object[] row : rows) {
            Vector<Object> v = new Vector<>();
            for (Object cell : row) v.add(cell);
            model.addRow(v);
        }
    }

    /** Returns the id (first column) of the selected row, or -1 after telling the user to select one. */
    protected int selectedId(JTable table, DefaultTableModel model) {
        int row = table.getSelectedRow();
        if (row == -1) {
            Messages.show(this, "Please select a row first.");
            return -1;
        }
        Integer id = (Integer) model.getValueAt(row, 0);      // wrapper class: Object -> Integer
        return id;                                            // unboxing to int
    }
}
