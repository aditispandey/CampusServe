package campusserve.model;

import java.sql.Timestamp;

/** One shift of an event (a row of the ShiftRole table). Shows all 3 kinds of constructor. */
public class ShiftRole implements Reportable {
    private int shiftId;
    private int eventId;
    private String eventName;
    private String roleName;
    private Timestamp startTime;
    private Timestamp endTime;
    private int slotsAvailable;
    private int slotsFilled;

    // 1) Default constructor (used by the DAO: create empty object, then fill with setters)
    public ShiftRole() {
        this.eventName = "";
        this.roleName = "Unnamed";
        this.slotsAvailable = 1;
    }

    // 2) Parameterized constructor (used when the coordinator adds a shift)
    public ShiftRole(int shiftId, int eventId, String eventName, String roleName,
                     Timestamp startTime, Timestamp endTime, int slotsAvailable, int slotsFilled) {
        this.shiftId = shiftId;
        this.eventId = eventId;
        this.eventName = eventName;
        this.roleName = roleName;
        this.startTime = startTime;
        this.endTime = endTime;
        this.slotsAvailable = slotsAvailable;
        this.slotsFilled = slotsFilled;
    }

    // 3) Copy constructor (used by "Duplicate Shift")
    public ShiftRole(ShiftRole other) {
        this(other.shiftId, other.eventId, other.eventName, other.roleName,
             other.startTime, other.endTime, other.slotsAvailable, other.slotsFilled);
    }

    public int getShiftId() { return shiftId; }
    public void setShiftId(int shiftId) { this.shiftId = shiftId; }
    public int getEventId() { return eventId; }
    public void setEventId(int eventId) { this.eventId = eventId; }
    public String getEventName() { return eventName; }
    public void setEventName(String eventName) { this.eventName = eventName; }
    public String getRoleName() { return roleName; }
    public void setRoleName(String roleName) { this.roleName = roleName; }
    public Timestamp getStartTime() { return startTime; }
    public void setStartTime(Timestamp startTime) { this.startTime = startTime; }
    public Timestamp getEndTime() { return endTime; }
    public void setEndTime(Timestamp endTime) { this.endTime = endTime; }
    public int getSlotsAvailable() { return slotsAvailable; }
    public void setSlotsAvailable(int slotsAvailable) { this.slotsAvailable = slotsAvailable; }
    public int getSlotsFilled() { return slotsFilled; }
    public void setSlotsFilled(int slotsFilled) { this.slotsFilled = slotsFilled; }

    public int getOpenSlots() { return slotsAvailable - slotsFilled; }

    /** Length of the shift in minutes. */
    public long getMinutes() { return (endTime.getTime() - startTime.getTime()) / 60000; }

    @Override
    public String getReport() {
        return roleName + " @ " + eventName + " (" + startTime + ") open slots: " + getOpenSlots();
    }
}
