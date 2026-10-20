package campusserve.ui;

import campusserve.dao.VolunteerCursor;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import javax.swing.*;

/** Shows ONE volunteer at a time and moves First / Previous / Next / Last through a scrollable ResultSet. Has a search box. */
public class VolunteerBrowserDialog extends JDialog {
    private VolunteerCursor cursor;
    private final JTextField searchField = new JTextField(14);
    private final JLabel nameLabel = new JLabel();
    private final JLabel emailLabel = new JLabel();
    private final JLabel deptLabel = new JLabel();
    private final JLabel hoursLabel = new JLabel();
    private final JLabel positionLabel = new JLabel(" ", SwingConstants.CENTER);

    public VolunteerBrowserDialog(JFrame owner) {
        super(owner, "Browse Volunteers", true);
        setSize(460, 320);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // ----- search bar -----
        JButton searchBtn = new JButton("Search");
        JButton clearBtn = new JButton("Clear");
        JPanel searchBar = new JPanel();
        searchBar.add(new JLabel("Search:"));
        searchBar.add(searchField);
        searchBar.add(searchBtn);
        searchBar.add(clearBtn);
        searchBtn.addActionListener(e -> search());
        searchField.addActionListener(e -> search());                  // Enter key also searches
        clearBtn.addActionListener(e -> { searchField.setText(""); search(); });

        JPanel details = new JPanel(new GridLayout(4, 2, 6, 6));
        details.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        details.add(new JLabel("Name:"));       details.add(nameLabel);
        details.add(new JLabel("Email:"));      details.add(emailLabel);
        details.add(new JLabel("Department:")); details.add(deptLabel);
        details.add(new JLabel("Hours:"));      details.add(hoursLabel);

        JButton first = new JButton("|<");
        JButton prev = new JButton("<");
        JButton next = new JButton(">");
        JButton last = new JButton(">|");
        JPanel nav = new JPanel();
        nav.add(first); nav.add(prev); nav.add(next); nav.add(last);

        JPanel bottom = new JPanel(new GridLayout(2, 1));
        bottom.add(positionLabel);
        bottom.add(nav);

        add(searchBar, BorderLayout.NORTH);
        add(details, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        try {
            cursor = new VolunteerCursor();
            first.addActionListener(e -> move("first"));
            prev.addActionListener(e -> move("previous"));
            next.addActionListener(e -> move("next"));
            last.addActionListener(e -> move("last"));
            move("first");
        } catch (SQLException ex) {
            Messages.error(owner, "Could not open volunteers: " + ex.getMessage());
        }

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (cursor != null) cursor.close();          // release the database connection
            }
        });
    }

    /** SEARCH: opens a new cursor with the text from the search box and shows its first record. */
    private void search() {
        try {
            VolunteerCursor fresh = new VolunteerCursor(searchField.getText());
            if (cursor != null) cursor.close();              // close the old cursor first
            cursor = fresh;
            move("first");
        } catch (SQLException ex) {
            Messages.error(this, "Search failed: " + ex.getMessage());
        }
    }

    private void move(String where) {
        try {
            if (cursor.getTotal() == 0) {
                nameLabel.setText("");
                emailLabel.setText("");
                deptLabel.setText("");
                hoursLabel.setText("");
                positionLabel.setText("No volunteers found.");
                return;
            }
            boolean moved;
            switch (where) {
                case "first":    moved = cursor.first(); break;
                case "previous": moved = cursor.previous(); break;
                case "next":     moved = cursor.next(); break;
                default:         moved = cursor.last(); break;
            }
            if (!moved) {                                    // fell off the start or the end: go back onto a real row
                if (where.equals("previous")) cursor.first(); else cursor.last();
            }
            nameLabel.setText(cursor.getName());
            emailLabel.setText(cursor.getEmail());
            deptLabel.setText(cursor.getDept());
            hoursLabel.setText(String.valueOf(cursor.getHours()));
            positionLabel.setText("Record " + cursor.getPosition() + " of " + cursor.getTotal());
        } catch (SQLException ex) {
            Messages.error(this, "Database error: " + ex.getMessage());
        }
    }
}