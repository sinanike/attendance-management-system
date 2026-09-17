package ir.ac.sutech.attendance_system.model;

public enum PerformanceReviewStatus {
    DRAFT("پیش‌نویس"),
    SUBMITTED("ارسال‌شده به منابع انسانی");

    private final String title;

    PerformanceReviewStatus(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
