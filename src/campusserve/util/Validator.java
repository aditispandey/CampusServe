package campusserve.util;

import campusserve.exceptions.InvalidInputException;
import java.sql.Date;
import java.sql.Timestamp;

/** Checks what the user typed. Every method 'throws' our own exception when input is bad. */
public final class Validator {
    private Validator() { }

    // OVERLOADED static methods
    public static void require(String field, String value) throws InvalidInputException {
        if (value == null || value.trim().isEmpty()) {
            throw new InvalidInputException(field + " cannot be empty.");          // 'throw'
        }
    }
    public static void require(String field, int value) throws InvalidInputException {
        if (value <= 0) {
            throw new InvalidInputException(field + " must be greater than 0.");
        }
    }

    /** Wrapper class: String -> Integer -> int. */
    public static int parseInt(String field, String text) throws InvalidInputException {
        try {
            Integer number = Integer.valueOf(text.trim());      // wrapper class
            return number;                                      // unboxing
        } catch (NumberFormatException e) {
            throw new InvalidInputException(field + " must be a whole number.");
        }
    }

    public static Date parseDate(String field, String text) throws InvalidInputException {
        try {
            return Date.valueOf(text.trim());                   // yyyy-MM-dd
        } catch (IllegalArgumentException e) {
            throw new InvalidInputException(field + " must look like 2026-11-10");
        }
    }

    public static Timestamp parseDateTime(String field, String text) throws InvalidInputException {
        try {
            return Timestamp.valueOf(text.trim() + ":00");      // yyyy-MM-dd HH:mm
        } catch (IllegalArgumentException e) {
            throw new InvalidInputException(field + " must look like 2026-11-10 09:00");
        }
    }

    public static void checkEmail(String email) throws InvalidInputException {
        require("Email", email);
        if (!email.contains("@") || !email.contains(".")) {
            throw new InvalidInputException("Email is not valid.");
        }
    }
}
