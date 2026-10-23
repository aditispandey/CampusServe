package campusserve.util;

import campusserve.model.Volunteer;
import java.util.List;

/** Builds the text of a volunteer's hours report using StringBuffer. */
public final class ReportBuilder {
    private ReportBuilder() { }

    public static String buildVolunteerReport(Volunteer v, double hours, List<Object[]> shifts, List<String> certificates) {
        StringBuffer sb = new StringBuffer();
        sb.append("Name        : ").append(v.getName()).append("\n");
        sb.append("Email       : ").append(v.getEmail()).append("\n");
        sb.append("Total hours : ").append(Rules.formatHours(hours)).append("\n");
        sb.append("To certificate: ").append(Rules.formatHours(Rules.hoursToCertificate(hours))).append(" more\n\n");

        sb.append("SHIFTS\n");
        if (shifts.isEmpty()) sb.append("  (none)\n");
        for (Object[] row : shifts) {
            sb.append("  - ").append(row[1]).append(" | ").append(row[2])
              .append(" | ").append(row[3]).append(" | ").append(row[4]).append(" hrs\n");
        }

        sb.append("\nCERTIFICATES\n");
        if (certificates.isEmpty()) sb.append("  (none yet)\n");
        for (String code : certificates) sb.append("  - ").append(code).append("\n");

        sb.insert(0, "===== CAMPUSSERVE VOLUNTEER REPORT =====\n\n");   // StringBuffer.insert
        return sb.toString();
    }
}
