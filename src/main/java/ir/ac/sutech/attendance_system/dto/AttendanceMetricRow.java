package ir.ac.sutech.attendance_system.dto;

import ir.ac.sutech.attendance_system.model.Employee;

public class AttendanceMetricRow {

    private final Employee employee;
    private final long presentDays;
    private final long absentDays;
    private final long incompleteDays;
    private final long workedMinutes;
    private final long overtimeMinutes;
    private final long lateMinutes;
    private final long earlyLeaveMinutes;
    private final double attendanceRate;

    public AttendanceMetricRow(
            Employee employee,
            long presentDays,
            long absentDays,
            long incompleteDays,
            long workedMinutes,
            long overtimeMinutes,
            long lateMinutes,
            long earlyLeaveMinutes,
            double attendanceRate
    ) {
        this.employee = employee;
        this.presentDays = presentDays;
        this.absentDays = absentDays;
        this.incompleteDays = incompleteDays;
        this.workedMinutes = workedMinutes;
        this.overtimeMinutes = overtimeMinutes;
        this.lateMinutes = lateMinutes;
        this.earlyLeaveMinutes = earlyLeaveMinutes;
        this.attendanceRate = attendanceRate;
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

    public long getWorkedMinutes() {
        return workedMinutes;
    }

    public long getOvertimeMinutes() {
        return overtimeMinutes;
    }

    public long getLateMinutes() {
        return lateMinutes;
    }

    public long getEarlyLeaveMinutes() {
        return earlyLeaveMinutes;
    }

    public double getAttendanceRate() {
        return attendanceRate;
    }

    public long getWorkedHours() {
        return workedMinutes / 60;
    }

    public long getWorkedRemainingMinutes() {
        return workedMinutes % 60;
    }

    public long getOvertimeHours() {
        return overtimeMinutes / 60;
    }

    public long getOvertimeRemainingMinutes() {
        return overtimeMinutes % 60;
    }

    public long getLateHours() {
        return lateMinutes / 60;
    }

    public long getLateRemainingMinutes() {
        return lateMinutes % 60;
    }

    public long getEarlyLeaveHours() {
        return earlyLeaveMinutes / 60;
    }

    public long getEarlyLeaveRemainingMinutes() {
        return earlyLeaveMinutes % 60;
    }

    public long getAttendanceDenominatorDays() {
        return presentDays + absentDays + incompleteDays;
    }
}
