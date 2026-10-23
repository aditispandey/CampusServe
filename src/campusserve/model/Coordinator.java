package campusserve.model;

public class Coordinator extends User {
    public Coordinator(int userId, String name, String email) {
        super(userId, name, email, "COORDINATOR");
    }

    @Override
    public String getDashboardTitle() { return "Coordinator Dashboard - " + name; }

    @Override
    public String getBasicInfo() {
        return "Coordinator: " + super.getBasicInfo();
    }
}
