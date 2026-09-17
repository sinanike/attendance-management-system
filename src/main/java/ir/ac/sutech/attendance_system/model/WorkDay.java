package ir.ac.sutech.attendance_system.model;

public enum WorkDay {

    SATURDAY("شنبه"),
    SUNDAY("یکشنبه"),
    MONDAY("دوشنبه"),
    TUESDAY("سه‌شنبه"),
    WEDNESDAY("چهارشنبه"),
    THURSDAY("پنجشنبه"),
    FRIDAY("جمعه");

    private final String persianTitle;

    WorkDay(String persianTitle) {
        this.persianTitle = persianTitle;
    }

    public String getPersianTitle() {
        return persianTitle;
    }
}