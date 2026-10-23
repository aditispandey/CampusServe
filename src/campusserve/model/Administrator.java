package campusserve.model;

public class Administrator extends User {
    public Administrator(int userId, String name, String email) {
        super(userId, name, email, "ADMIN");
    }

    @Override
    public String getDashboardTitle() { return "Administrator Dashboard - " + name; }

    @Override
    public String getReport() { return "Admin: " + super.getReport(); }
}
