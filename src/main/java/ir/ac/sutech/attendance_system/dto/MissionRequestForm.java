package ir.ac.sutech.attendance_system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class MissionRequestForm {

    private Long employeeId;

    @NotNull(message = "تاریخ شروع مأموریت را وارد کنید")
    private LocalDate startDate;

    @NotNull(message = "تاریخ پایان مأموریت را وارد کنید")
    private LocalDate endDate;

    @NotBlank(message = "مقصد مأموریت را وارد کنید")
    @Size(
            max = 200,
            message = "مقصد نباید بیشتر از ۲۰۰ کاراکتر باشد"
    )
    private String destination;

    @NotBlank(message = "شرح مأموریت را وارد کنید")
    @Size(
            max = 1000,
            message = "شرح مأموریت نباید بیشتر از ۱۰۰۰ کاراکتر باشد"
    )
    private String purpose;


    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(
            Long employeeId
    ) {
        this.employeeId = employeeId;
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


    public String getDestination() {
        return destination;
    }

    public void setDestination(
            String destination
    ) {
        this.destination = destination;
    }


    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(
            String purpose
    ) {
        this.purpose = purpose;
    }
}