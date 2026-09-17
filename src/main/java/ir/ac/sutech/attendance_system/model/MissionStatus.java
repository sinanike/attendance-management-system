package ir.ac.sutech.attendance_system.model;

public enum MissionStatus {

    PENDING("در انتظار بررسی"),
    APPROVED("تأیید شده"),
    REJECTED("رد شده");

    private final String displayName;

    MissionStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getTitle() {
        return displayName;
    }
}