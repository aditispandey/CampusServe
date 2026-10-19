package campusserve.ui;

import campusserve.dao.UserDAO;
import campusserve.exceptions.InvalidInputException;
import campusserve.model.*;
import campusserve.util.ActivityLog;
import campusserve.util.Validator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;

/** Login window. Normally started from Main. */
public class LoginFrame extends JFrame {
    private final JTextField emailField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();

    public LoginFrame() {
        setTitle("CampusServe - Login");
        setSize(400, 280);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(5, 2, 8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JButton loginButton = new JButton("Log In");
        loginButton.addActionListener(new LoginListener());               // INNER class

        JButton registerButton = new JButton("Register (Volunteer)");
        registerButton.addActionListener(new ActionListener() {           // ANONYMOUS class
            @Override
            public void actionPerformed(ActionEvent e) { registerNewVolunteer(); }
        });

        JButton noticeButton = new JButton("Notice Board (Applet)");
        noticeButton.addActionListener(e -> openNoticeBoard());           // lambda

        panel.add(new JLabel("Email:"));
        panel.add(emailField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
        panel.add(new JLabel());
        panel.add(loginButton);
        panel.add(new JLabel());
        panel.add(registerButton);
        panel.add(new JLabel());
        panel.add(noticeButton);
        add(panel);
    }

    /** INNER CLASS: can use the private fields of LoginFrame directly. */
    private class LoginListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                Validator.checkEmail(emailField.getText().trim());
                Validator.require("Password", new String(passwordField.getPassword()));

                User user = new UserDAO().login(emailField.getText().trim(), new String(passwordField.getPassword()));
                if (user == null) {
                    Messages.show(LoginFrame.this, "Wrong email or password.", JOptionPane.WARNING_MESSAGE);
                    ActivityLog.log("LOGIN FAILED " + emailField.getText());
                    return;
                }
                ActivityLog.log("LOGIN " + user.getReport());
                openDashboard(user);
            } catch (InvalidInputException ex) {
                Messages.show(LoginFrame.this, ex.getMessage(), JOptionPane.WARNING_MESSAGE);
            } catch (SQLException ex) {
                Messages.error(LoginFrame.this, "Database error: " + ex.getMessage());
            }
        }
    }

    private void openDashboard(User user) {
        dispose();
        if (user instanceof Volunteer) {
            new VolunteerDashboard((Volunteer) user).setVisible(true);
        } else if (user instanceof Coordinator) {
            new CoordinatorDashboard((Coordinator) user).setVisible(true);
        } else {
            new AdminDashboard((Administrator) user).setVisible(true);
        }
    }

    private void registerNewVolunteer() {
        JTextField name = new JTextField();
        JTextField email = new JTextField();
        JPasswordField pass = new JPasswordField();
        Object[] form = { "Name:", name, "Email:", email, "Password:", pass };
        if (JOptionPane.showConfirmDialog(this, form, "Register", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
        try {
            Validator.require("Name", name.getText());
            Validator.checkEmail(email.getText().trim());
            Validator.require("Password", new String(pass.getPassword()));
            new UserDAO().registerVolunteer(name.getText().trim(), email.getText().trim(), new String(pass.getPassword()));
            ActivityLog.log("REGISTERED volunteer " + email.getText().trim());
            Messages.show(this, "Registered! You can log in now.");
        } catch (InvalidInputException ex) {
            Messages.show(this, ex.getMessage(), JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            Messages.error(this, "Could not register (email already used?): " + ex.getMessage());
        }
    }

    /** Applets no longer run in browsers, so a normal window hosts the applet and calls its life-cycle methods. */
    @SuppressWarnings({"deprecation", "removal"})
    private void openNoticeBoard() {
        final NoticeApplet applet = new NoticeApplet();
        final JFrame frame = new JFrame("CampusServe Notice Board");
        frame.add(applet, BorderLayout.CENTER);
        frame.setSize(450, 230);
        frame.setLocationRelativeTo(this);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        applet.init();
        applet.start();
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                applet.stop();
                applet.destroy();
            }
        });
        frame.setVisible(true);
    }
}
