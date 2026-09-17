package ir.ac.sutech.attendance_system.model;

public enum EmployeeType {

    ADMINISTRATIVE("کارمند اداری"),
    SERVICE_SUPPORT("خدمات و پشتیبانی");

    private final String displayName;

    EmployeeType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /** برای سازگاری با قالب‌های Thymeleaf که از title استفاده می‌کنند */
    public String getTitle() {
        return displayName;
    }
}