package campusserve.model;

public class Volunteer extends User {
    private int volunteerId;
    private double totalHoursServed;

    // static variable: one copy shared by ALL Volunteer objects
    private static int volunteerCount = 0;

    // 1) Default constructor
    public Volunteer() {
        super(0, "Unknown", "unknown@campus.edu", "VOLUNTEER");   // super(...) calls the parent constructor
        this.volunteerId = 0;
        this.totalHoursServed = 0;
        volunteerCount++;
    }

    // 2) Parameterized constructor
    public Volunteer(int userId, String name, String email, int volunteerId, double totalHoursServed) {
        super(userId, name, email, "VOLUNTEER");
        this.volunteerId = volunteerId;
        this.totalHoursServed = totalHoursServed;
        volunteerCount++;
    }

    // 3) Copy constructor
    public Volunteer(Volunteer other) {
        super(other.userId, other.name, other.email, "VOLUNTEER");
        this.volunteerId = other.volunteerId;
        this.totalHoursServed = other.totalHoursServed;
        volunteerCount++;
    }

    public int getVolunteerId() { return volunteerId; }
    public double getTotalHoursServed() { return totalHoursServed; }

    // METHOD OVERLOADING: same name, different parameters
    public void addHours(double hours) {
        totalHoursServed += hours;
    }
    public void addHours(int hours, int minutes) {
        totalHoursServed += hours + (minutes / 60.0);
    }

    // static method
    public static int getVolunteerCount() { return volunteerCount; }

    @Override
    public String getDashboardTitle() {
        return "Volunteer Dashboard - " + name;
    }

    @Override
    public String getReport() {
        return super.getReport() + " | hours = " + totalHoursServed;     // super.method() calls the parent's version
    }
}
