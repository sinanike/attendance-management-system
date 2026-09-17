package ir.ac.sutech.attendance_system.model;

public enum LeaveStatus {

    PENDING("در انتظار بررسی"),
    APPROVED("تأیید شده"),
    REJECTED("رد شده");

    private final String displayName;

    LeaveStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getTitle() {
        return displayName;
    }
}