package ir.ac.sutech.attendance_system.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public class AttendanceSettingsForm {

    private boolean manualCorrectionEnabled = true;

    @Min(
            value = 0,
            message = "حداقل بازه اصلاح صفر روز است"
    )
    @Max(
            value = 365,
            message = "حداکثر بازه اصلاح ۳۶۵ روز است"
    )
    private int maxCorrectionDays = 30;

    private boolean allowManualRecordDeletion = true;

    @Size(
            max = 500,
            message = "یادداشت تنظیمات حداکثر ۵۰۰ کاراکتر است"
    )
    private String note;

    public boolean isManualCorrectionEnabled() {
        return manualCorrectionEnabled;
    }

    public void setManualCorrectionEnabled(
            boolean manualCorrectionEnabled
    ) {
        this.manualCorrectionEnabled =
                manualCorrectionEnabled;
    }

    public int getMaxCorrectionDays() {
        return maxCorrectionDays;
    }

    public void setMaxCorrectionDays(
            int maxCorrectionDays
    ) {
        this.maxCorrectionDays =
                maxCorrectionDays;
    }

    public boolean isAllowManualRecordDeletion() {
        return allowManualRecordDeletion;
    }

    public void setAllowManualRecordDeletion(
            boolean allowManualRecordDeletion
    ) {
        this.allowManualRecordDeletion =
                allowManualRecordDeletion;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
