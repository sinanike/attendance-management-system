package ir.ac.sutech.attendance_system.dto;

import ir.ac.sutech.attendance_system.model.WorkDay;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalTime;

public class ShiftDayScheduleRow {

    private WorkDay workDay;
    private boolean workingDay;

    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime endTime;

    private Integer breakMinutes = 0;
    private Integer lateGraceMinutes = 0;
    private Integer earlyLeaveGraceMinutes = 0;

    public WorkDay getWorkDay() {
        return workDay;
    }

    public void setWorkDay(WorkDay workDay) {
        this.workDay = workDay;
    }

    public boolean isWorkingDay() {
        return workingDay;
    }

    public void setWorkingDay(boolean workingDay) {
        this.workingDay = workingDay;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public Integer getBreakMinutes() {
        return breakMinutes;
    }

    public void setBreakMinutes(Integer breakMinutes) {
        this.breakMinutes = breakMinutes;
    }

    public Integer getLateGraceMinutes() {
        return lateGraceMinutes;
    }

    public void setLateGraceMinutes(Integer lateGraceMinutes) {
        this.lateGraceMinutes = lateGraceMinutes;
    }

    public Integer getEarlyLeaveGraceMinutes() {
        return earlyLeaveGraceMinutes;
    }

    public void setEarlyLeaveGraceMinutes(Integer earlyLeaveGraceMinutes) {
        this.earlyLeaveGraceMinutes = earlyLeaveGraceMinutes;
    }
}