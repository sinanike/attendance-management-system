package ir.ac.sutech.attendance_system.dto;

import ir.ac.sutech.attendance_system.model.AttendanceType;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

public class AttendanceRecordForm {

    @NotNull(message = "انتخاب کارمند الزامی است")
    private Long employeeId;

    @NotNull(message = "تاریخ و ساعت تردد الزامی است")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime eventTime;

    @NotNull(message = "نوع تردد را انتخاب کنید")
    private AttendanceType attendanceType;

    public AttendanceRecordForm() {
        this.eventTime = LocalDateTime.now()
                .withSecond(0)
                .withNano(0);
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public LocalDateTime getEventTime() {
        return eventTime;
    }

    public void setEventTime(LocalDateTime eventTime) {
        this.eventTime = eventTime;
    }

    public AttendanceType getAttendanceType() {
        return attendanceType;
    }

    public void setAttendanceType(
            AttendanceType attendanceType
    ) {
        this.attendanceType = attendanceType;
    }
}