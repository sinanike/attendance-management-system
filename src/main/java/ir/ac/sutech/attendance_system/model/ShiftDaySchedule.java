package ir.ac.sutech.attendance_system.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalTime;

@Entity
@Table(
        name = "shift_day_schedules",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_shift_day_schedule",
                columnNames = {"shift_id", "work_day"}
        )
)
public class ShiftDaySchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "shift_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_day_schedule_shift")
    )
    private Shift shift;

    @Enumerated(EnumType.STRING)
    @Column(name = "work_day", nullable = false, length = 20)
    private WorkDay workDay;

    @Column(name = "working_day", nullable = false)
    private boolean workingDay;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "break_minutes", nullable = false)
    private int breakMinutes;

    @Column(name = "late_grace_minutes", nullable = false)
    private int lateGraceMinutes;

    @Column(name = "early_leave_grace_minutes", nullable = false)
    private int earlyLeaveGraceMinutes;

    protected ShiftDaySchedule() {
    }

    public ShiftDaySchedule(Shift shift, WorkDay workDay) {
        this.shift = shift;
        this.workDay = workDay;
    }

    public Long getId() {
        return id;
    }

    public Shift getShift() {
        return shift;
    }

    public WorkDay getWorkDay() {
        return workDay;
    }

    public boolean isWorkingDay() {
        return workingDay;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public int getBreakMinutes() {
        return breakMinutes;
    }

    public int getLateGraceMinutes() {
        return lateGraceMinutes;
    }

    public int getEarlyLeaveGraceMinutes() {
        return earlyLeaveGraceMinutes;
    }

    public void setShift(Shift shift) {
        this.shift = shift;
    }

    public void setWorkDay(WorkDay workDay) {
        this.workDay = workDay;
    }

    public void setWorkingDay(boolean workingDay) {
        this.workingDay = workingDay;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public void setBreakMinutes(int breakMinutes) {
        this.breakMinutes = breakMinutes;
    }

    public void setLateGraceMinutes(int lateGraceMinutes) {
        this.lateGraceMinutes = lateGraceMinutes;
    }

    public void setEarlyLeaveGraceMinutes(int earlyLeaveGraceMinutes) {
        this.earlyLeaveGraceMinutes = earlyLeaveGraceMinutes;
    }

    public boolean crossesMidnight() {
        return workingDay
                && startTime != null
                && endTime != null
                && !endTime.isAfter(startTime);
    }
}