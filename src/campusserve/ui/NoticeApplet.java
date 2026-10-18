package campusserve.ui;

import campusserve.dao.ShiftDAO;
import campusserve.model.ShiftRole;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;

/** APPLET (JApplet = Swing applet). Life cycle: init() -> start() -> stop() -> destroy(). Shows open shifts to everyone. */
@SuppressWarnings({"deprecation", "removal"})
public class NoticeApplet extends JApplet {
    private final JTextArea board = new JTextArea();

    @Override
    public void init() {
        setLayout(new BorderLayout());
        JLabel title = new JLabel("CampusServe Notice Board - Open Shifts", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        board.setEditable(false);
        JButton reload = new JButton("Reload");
        reload.addActionListener(e -> loadShifts());
        add(title, BorderLayout.NORTH);
        add(new JScrollPane(board), BorderLayout.CENTER);
        add(reload, BorderLayout.SOUTH);
    }

    @Override
    public void start() { loadShifts(); }

    @Override
    public void stop() { System.out.println("NoticeApplet stop()"); }

    @Override
    public void destroy() { System.out.println("NoticeApplet destroy()"); }

    private void loadShifts() {
        try {
            ArrayList<ShiftRole> open = new ShiftDAO().getOpenShifts();
            StringBuffer sb = new StringBuffer();
            for (ShiftRole s : open) sb.append("* ").append(s.getReport()).append("\n");
            board.setText(open.isEmpty() ? "No open shifts right now." : sb.toString());
        } catch (SQLException e) {
            board.setText("Could not load shifts: " + e.getMessage());
        }
    }
}
