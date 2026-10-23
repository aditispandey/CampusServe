package campusserve.exceptions;

/** User-defined exception: something typed in a form is wrong. */
public class InvalidInputException extends Exception {
    public InvalidInputException(String message) { super(message); }
}
