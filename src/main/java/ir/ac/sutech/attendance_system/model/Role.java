package ir.ac.sutech.attendance_system.model;

public enum Role {

    ADMIN("مدیر امور اداری"),
    OPERATOR("اپراتور حضور و غیاب"),
    MANAGER("مدیر بخش"),
    PRESIDENT("ریاست دانشگاه"),
    EMPLOYEE("کارمند");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}