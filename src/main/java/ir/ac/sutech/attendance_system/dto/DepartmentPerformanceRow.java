package ir.ac.sutech.attendance_system.dto;

public class DepartmentPerformanceRow {

    private final Long departmentId;
    private final String departmentCode;
    private final String departmentName;

    private final long employeeCount;

    private final long presentDays;
    private final long absentDays;
    private final long incompleteDays;
    private final long leaveDays;
    private final long missionDays;

    private final long totalWorkedMinutes;
    private final long totalLateMinutes;
    private final long totalEarlyLeaveMinutes;

    private final double attendanceRate;

    public DepartmentPerformanceRow(
            Long departmentId,
            String departmentCode,
            String departmentName,
            long employeeCount,
            long presentDays,
            long absentDays,
            long incompleteDays,
            long leaveDays,
            long missionDays,
            long totalWorkedMinutes,
            long totalLateMinutes,
            long totalEarlyLeaveMinutes
    ) {

        this.departmentId =
                departmentId;

        this.departmentCode =
                departmentCode;

        this.departmentName =
                departmentName;

        this.employeeCount =
                employeeCount;

        this.presentDays =
                presentDays;

        this.absentDays =
                absentDays;

        this.incompleteDays =
                incompleteDays;

        this.leaveDays =
                leaveDays;

        this.missionDays =
                missionDays;

        this.totalWorkedMinutes =
                totalWorkedMinutes;

        this.totalLateMinutes =
                totalLateMinutes;

        this.totalEarlyLeaveMinutes =
                totalEarlyLeaveMinutes;

        long denominator =
                presentDays
                        +
                        absentDays
                        +
                        incompleteDays;

        this.attendanceRate =
                denominator == 0
                        ? 0
                        : (
                        presentDays
                                *
                                100.0
                                /
                                denominator
                );
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public String getDepartmentCode() {
        return departmentCode;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public long getEmployeeCount() {
        return employeeCount;
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

    public long getTotalWorkedMinutes() {
        return totalWorkedMinutes;
    }

    public long getTotalLateMinutes() {
        return totalLateMinutes;
    }

    public long getTotalEarlyLeaveMinutes() {
        return totalEarlyLeaveMinutes;
    }

    public double getAttendanceRate() {
        return attendanceRate;
    }

    public long getWorkedHours() {
        return totalWorkedMinutes / 60;
    }

    public long getWorkedRemainingMinutes() {
        return totalWorkedMinutes % 60;
    }

    public long getLateHours() {
        return totalLateMinutes / 60;
    }

    public long getLateRemainingMinutes() {
        return totalLateMinutes % 60;
    }

    public long getEarlyLeaveHours() {
        return totalEarlyLeaveMinutes / 60;
    }

    public long getEarlyLeaveRemainingMinutes() {
        return totalEarlyLeaveMinutes % 60;
    }
}
