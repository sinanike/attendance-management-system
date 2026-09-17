package ir.ac.sutech.attendance_system.model;

public enum AttendanceType {

    ENTRY("ورود"),
    EXIT("خروج");

    private final String displayName;

    AttendanceType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}