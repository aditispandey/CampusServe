package campusserve.util;

import java.io.*;
import java.time.LocalDateTime;

/** FILE HANDLING: writes every important action to campusserve_log.txt and can read it back. */
public final class ActivityLog {
    private static final String FILE_NAME = "campusserve_log.txt";

    private ActivityLog() { }

    /** 'synchronized': several threads may log at once, but only one writes at a time. */
    public static synchronized void log(String message) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(FILE_NAME, true));      // true = append
            writer.write(LocalDateTime.now().withNano(0) + " | " + message);
            writer.newLine();
        } catch (IOException e) {
            System.out.println("Could not write log: " + e.getMessage());
        } finally {                                      // finally: always runs
            try {                                        // nested try-catch
                if (writer != null) writer.close();
            } catch (IOException e) {
                System.out.println("Could not close log file.");
            }
        }
    }

    /** Reads the whole log file. 'throws' passes the problem to the caller. */
    public static synchronized String readAll() throws IOException {
        File file = new File(FILE_NAME);
        if (!file.exists()) return "(no activity logged yet)";
        StringBuffer content = new StringBuffer();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }
}
