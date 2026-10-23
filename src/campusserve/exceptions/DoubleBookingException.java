package campusserve.exceptions;

/** User-defined exception: volunteer already has an overlapping shift. */
public class DoubleBookingException extends Exception {
    public DoubleBookingException(String message) { super(message); }
}
