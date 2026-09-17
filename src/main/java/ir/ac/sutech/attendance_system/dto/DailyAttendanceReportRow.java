package ir.ac.sutech.attendance_system.dto;

import ir.ac.sutech.attendance_system.model.Employee;
import ir.ac.sutech.attendance_system.model.Shift;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class DailyAttendanceReportRow {

    private final Employee employee;
    private final LocalDate reportDate;
    private final Shift shift;

    private final LocalDateTime scheduledStart;
    private final LocalDateTime scheduledEnd;

    private final LocalDateTime firstCheckIn;
    private final LocalDateTime lastCheckOut;

    private final long lateMinutes;
    private final long earlyLeaveMinutes;
    private final long workedMinutes;
    private final long requiredMinutes;
    private final long overtimeMinutes;

    private final int breakMinutes;

    private final DailyAttendanceStatus status;

    public DailyAttendanceReportRow(
            Employee employee,
            LocalDate reportDate,
            Shift shift,
            LocalDateTime scheduledStart,
            LocalDateTime scheduledEnd,
            LocalDateTime firstCheckIn,
            LocalDateTime lastCheckOut,
            long lateMinutes,
            long earlyLeaveMinutes,
            int breakMinutes,
            DailyAttendanceStatus status
    ) {
        this.employee = employee;
        this.reportDate = reportDate;
        this.shift = shift;
        this.scheduledStart = scheduledStart;
        this.scheduledEnd = scheduledEnd;
        this.firstCheckIn = firstCheckIn;
        this.lastCheckOut = lastCheckOut;
        this.lateMinutes = Math.max(lateMinutes, 0);
        this.earlyLeaveMinutes =
                Math.max(earlyLeaveMinutes, 0);
        this.breakMinutes = Math.max(breakMinutes, 0);

        this.workedMinutes =
                calculateWorkedMinutes();

        this.requiredMinutes =
                calculateRequiredMinutes();

        this.overtimeMinutes =
                Math.max(
                        workedMinutes - requiredMinutes,
                        0
                );

        this.status = status;
    }

    private long calculateWorkedMinutes() {
        if (firstCheckIn == null
                || lastCheckOut == null) {

            return 0;
        }

        if (lastCheckOut.isBefore(firstCheckIn)) {
            return 0;
        }

        return Math.max(
                Duration.between(
                        firstCheckIn,
                        lastCheckOut
                ).toMinutes() - breakMinutes,
                0
        );
    }

    private long calculateRequiredMinutes() {
        if (scheduledStart == null
                || scheduledEnd == null) {

            return 0;
        }

        return Math.max(
                Duration.between(
                        scheduledStart,
                        scheduledEnd
                ).toMinutes() - breakMinutes,
                0
        );
    }

    public Employee getEmployee() {
        return employee;
    }

    public LocalDate getReportDate() {
        return reportDate;
    }

    public Shift getShift() {
        return shift;
    }

    public LocalDateTime getScheduledStart() {
        return scheduledStart;
    }

    public LocalDateTime getScheduledEnd() {
        return scheduledEnd;
    }

    public LocalDateTime getFirstCheckIn() {
        return firstCheckIn;
    }

    public LocalDateTime getLastCheckOut() {
        return lastCheckOut;
    }

    public long getLateMinutes() {
        return lateMinutes;
    }

    public long getEarlyLeaveMinutes() {
        return earlyLeaveMinutes;
    }

    public long getWorkedMinutes() {
        return workedMinutes;
    }

    public long getRequiredMinutes() {
        return requiredMinutes;
    }

    public long getOvertimeMinutes() {
        return overtimeMinutes;
    }

    public int getBreakMinutes() {
        return breakMinutes;
    }

    public String getOvertimeTimeText() {
        return (overtimeMinutes / 60)
                + " ساعت و "
                + (overtimeMinutes % 60)
                + " دقیقه";
    }

    public long getWorkedHours() {
        return workedMinutes / 60;
    }

    public long getWorkedRemainingMinutes() {
        return workedMinutes % 60;
    }

    public String getWorkedTimeText() {
        if (!hasCheckIn() || !hasCheckOut()) {
            return "—";
        }

        return getWorkedHours()
                + " ساعت و "
                + getWorkedRemainingMinutes()
                + " دقیقه";
    }

    public DailyAttendanceStatus getStatus() {
        return status;
    }

    public String getStatusTitle() {
        return status.getTitle();
    }

    public boolean isLate() {
        return lateMinutes > 0;
    }

    public boolean isEarlyLeave() {
        return earlyLeaveMinutes > 0;
    }

    public boolean hasCheckIn() {
        return firstCheckIn != null;
    }

    public boolean hasCheckOut() {
        return lastCheckOut != null;
    }
}