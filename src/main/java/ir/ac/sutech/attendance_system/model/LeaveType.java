package ir.ac.sutech.attendance_system.model;

public enum LeaveType {

    DAILY("مرخصی روزانه"),
    HOURLY("مرخصی ساعتی"),
    SICK("مرخصی استعلاجی"),
    OTHER("سایر");

    private final String displayName;

    LeaveType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getTitle() {
        return displayName;
    }
}