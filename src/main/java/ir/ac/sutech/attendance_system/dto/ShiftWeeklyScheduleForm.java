package ir.ac.sutech.attendance_system.dto;

import java.util.ArrayList;
import java.util.List;

public class ShiftWeeklyScheduleForm {

    private List<ShiftDayScheduleRow> days = new ArrayList<>();

    public List<ShiftDayScheduleRow> getDays() {
        return days;
    }

    public void setDays(List<ShiftDayScheduleRow> days) {
        this.days = days;
    }
}