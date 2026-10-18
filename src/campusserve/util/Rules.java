package campusserve.util;

/** 'final' class: cannot be extended. Business rules + static helper methods. */
public final class Rules {
    public static final double CERTIFICATE_THRESHOLD = 10.0;     // 'final' variable: value never changes

    private Rules() { }

    public static boolean isCertificateEligible(double hours) {
        return hours >= CERTIFICATE_THRESHOLD;
    }

    public static double hoursToCertificate(double hours) {
        return Math.max(0, CERTIFICATE_THRESHOLD - hours);
    }

    // static METHOD OVERLOADING
    public static String formatHours(double hours) {
        return hours + " hrs";
    }
    public static String formatHours(int hours, int minutes) {
        return hours + " h " + minutes + " min";
    }
}
