package ir.ac.sutech.attendance_system.dto;

import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public class EmployeeShiftAssignmentForm {

    @NotNull(
            message = "انتخاب کارمند الزامی است"
    )
    private Long employeeId;

    @NotNull(
            message = "انتخاب شیفت الزامی است"
    )
    private Long shiftId;

    @NotNull(
            message = "تاریخ شروع الزامی است"
    )
    @DateTimeFormat(
            iso = DateTimeFormat.ISO.DATE
    )
    private LocalDate startDate;

    /*
     * اگر خالی باشد،
     * Service همان startDate را به عنوان پایان
     * قرار می‌دهد؛ یعنی شیفت فقط برای یک روز است.
     */
    @DateTimeFormat(
            iso = DateTimeFormat.ISO.DATE
    )
    private LocalDate endDate;

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(
            Long employeeId
    ) {
        this.employeeId = employeeId;
    }

    public Long getShiftId() {
        return shiftId;
    }

    public void setShiftId(
            Long shiftId
    ) {
        this.shiftId = shiftId;
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
}