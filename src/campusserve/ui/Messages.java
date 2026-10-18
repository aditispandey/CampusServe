package campusserve.ui;

import javax.swing.JOptionPane;
import java.awt.Component;

/** Static helper for pop-up messages. show() is OVERLOADED. */
public final class Messages {
    private static int shownCount = 0;           // static variable

    private Messages() { }

    public static void show(Component parent, String text) {
        show(parent, text, JOptionPane.INFORMATION_MESSAGE);
    }

    public static void show(Component parent, String text, int type) {
        shownCount++;
        JOptionPane.showMessageDialog(parent, text, "CampusServe", type);
    }

    public static void error(Component parent, String text) {
        show(parent, text, JOptionPane.ERROR_MESSAGE);
    }

    public static int getShownCount() { return shownCount; }
}
