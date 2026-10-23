package campusserve.exceptions;

/** User-defined exception: shift is full. */
public class InsufficientSlotsException extends Exception {
    public InsufficientSlotsException(String message) { super(message); }
}
