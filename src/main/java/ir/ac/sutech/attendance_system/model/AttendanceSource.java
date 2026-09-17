package ir.ac.sutech.attendance_system.model;

public enum AttendanceSource {

    MANUAL("ثبت دستی"),
    FINGERPRINT("دستگاه اثر انگشت"),
    FILE_IMPORT("فایل دستگاه");

    private final String displayName;

    AttendanceSource(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}