package campusserve.model;

/** Abstract parent class of Volunteer, Coordinator and Administrator. Implements the Reportable interface. */
public abstract class User implements Reportable {
    protected int userId;
    protected String name;
    protected String email;
    protected String role;

    public User(int userId, String name, String email, String role) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public int getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }

    /** 'final' method: children may NOT override it. */
    public final String getRole() { return role; }

    public String getBasicInfo() {
        return name + " <" + email + "> [" + role + "]";
    }

    @Override
    public String getReport() { return getBasicInfo(); }

    /** Abstract method: every child MUST write its own version. */
    public abstract String getDashboardTitle();
}
