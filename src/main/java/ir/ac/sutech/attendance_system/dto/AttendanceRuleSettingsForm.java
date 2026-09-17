package ir.ac.sutech.attendance_system.dto;

public class AttendanceRuleSettingsForm {

    private int annualLeaveDays;
    private double minimumAttendancePercent;

    public int getAnnualLeaveDays() {
        return annualLeaveDays;
    }

    public void setAnnualLeaveDays(int annualLeaveDays) {
        this.annualLeaveDays = annualLeaveDays;
    }

    public double getMinimumAttendancePercent() {
        return minimumAttendancePercent;
    }

    public void setMinimumAttendancePercent(double minimumAttendancePercent) {
        this.minimumAttendancePercent = minimumAttendancePercent;
    }
}
