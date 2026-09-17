package ir.ac.sutech.attendance_system.dto;

import ir.ac.sutech.attendance_system.model.Employee;

public class AttendanceSummaryReportRow {

    private final Employee employee;

    private final long presentDays;
    private final long absentDays;
    private final long incompleteDays;

    private final long leaveDays;
    private final long missionDays;
    private final long holidayDays;

    private final long noShiftDays;

    private final long totalWorkedMinutes;
    private final long totalLateMinutes;
    private final long totalEarlyLeaveMinutes;

    public AttendanceSummaryReportRow(
            Employee employee,
            long presentDays,
            long absentDays,
            long incompleteDays,
            long leaveDays,
            long missionDays,
            long holidayDays,
            long noShiftDays,
            long totalWorkedMinutes,
            long totalLateMinutes,
            long totalEarlyLeaveMinutes
    ) {

        this.employee = employee;

        this.presentDays = presentDays;
        this.absentDays = absentDays;
        this.incompleteDays = incompleteDays;

        this.leaveDays = leaveDays;
        this.missionDays = missionDays;
        this.holidayDays = holidayDays;

        this.noShiftDays = noShiftDays;

        this.totalWorkedMinutes =
                totalWorkedMinutes;

        this.totalLateMinutes =
                totalLateMinutes;

        this.totalEarlyLeaveMinutes =
                totalEarlyLeaveMinutes;
    }

    public Employee getEmployee() {
        return employee;
    }

    public long getPresentDays() {
        return presentDays;
    }

    public long getAbsentDays() {
        return absentDays;
    }

    public long getIncompleteDays() {
        return incompleteDays;
    }

    public long getLeaveDays() {
        return leaveDays;
    }

    public long getMissionDays() {
        return missionDays;
    }

    public long getHolidayDays() {
        return holidayDays;
    }

    public long getNoShiftDays() {
        return noShiftDays;
    }

    public long getTotalWorkedMinutes() {
        return totalWorkedMinutes;
    }

    public long getTotalLateMinutes() {
        return totalLateMinutes;
    }

    public long getTotalEarlyLeaveMinutes() {
        return totalEarlyLeaveMinutes;
    }

    public long getTotalWorkedHours() {
        return totalWorkedMinutes / 60;
    }

    public long getTotalWorkedRemainingMinutes() {
        return totalWorkedMinutes % 60;
    }

    public long getTotalLateHours() {
        return totalLateMinutes / 60;
    }

    public long getTotalLateRemainingMinutes() {
        return totalLateMinutes % 60;
    }

    public long getTotalEarlyLeaveHours() {
        return totalEarlyLeaveMinutes / 60;
    }

    public long getTotalEarlyLeaveRemainingMinutes() {
        return totalEarlyLeaveMinutes % 60;
    }
}