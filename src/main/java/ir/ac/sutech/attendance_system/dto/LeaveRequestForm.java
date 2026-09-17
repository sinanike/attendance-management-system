package ir.ac.sutech.attendance_system.dto;

import ir.ac.sutech.attendance_system.model.LeaveType;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;

public class LeaveRequestForm {

    private Long employeeId;

    @NotNull(message = "نوع مرخصی را انتخاب کنید")
    private LeaveType leaveType;

    @NotNull(message = "تاریخ شروع را وارد کنید")
    private LocalDate startDate;

    @NotNull(message = "تاریخ پایان را وارد کنید")
    private LocalDate endDate;

    private LocalTime startTime;

    private LocalTime endTime;

    @Size(
            max = 1000,
            message = "دلیل درخواست نباید بیشتر از ۱۰۰۰ کاراکتر باشد"
    )
    private String reason;


    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(
            Long employeeId
    ) {
        this.employeeId = employeeId;
    }


    public LeaveType getLeaveType() {
        return leaveType;
    }

    public void setLeaveType(
            LeaveType leaveType
    ) {
        this.leaveType = leaveType;
    }


    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(
            LocalDate startDate
    ) {
        this.startDate = startDate;
    }


    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(
            LocalDate endDate
    ) {
        this.endDate = endDate;
    }


    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(
            LocalTime startTime
    ) {
        this.startTime = startTime;
    }


    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(
            LocalTime endTime
    ) {
        this.endTime = endTime;
    }


    public String getReason() {
        return reason;
    }

    public void setReason(
            String reason
    ) {
        this.reason = reason;
    }
}