package ir.ac.sutech.attendance_system.dto;

public enum DailyAttendanceStatus {

    PRESENT("حاضر"),

    ABSENT("غایب"),

    INCOMPLETE("تردد ناقص"),

    LEAVE("مرخصی"),

    MISSION("مأموریت"),

    HOLIDAY("تعطیل رسمی"),

    NO_SHIFT("بدون شیفت");

    private final String title;

    DailyAttendanceStatus(
            String title
    ) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}