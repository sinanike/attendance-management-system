package ir.ac.sutech.attendance_system.dto;

import ir.ac.sutech.attendance_system.model.WorkDay;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalTime;
import java.util.EnumSet;
import java.util.Set;

public class ShiftForm {

    @NotBlank(message = "کد شیفت را وارد کنید")
    @Size(
            max = 20,
            message = "کد شیفت نمی‌تواند بیشتر از ۲۰ کاراکتر باشد"
    )
    @Pattern(
            regexp = "^[A-Za-z0-9_-]+$",
            message = "کد شیفت فقط می‌تواند شامل حروف انگلیسی، عدد، خط تیره و زیرخط باشد"
    )
    private String shiftCode;

    @NotBlank(message = "نام شیفت را وارد کنید")
    @Size(
            max = 70,
            message = "نام شیفت نمی‌تواند بیشتر از ۷۰ کاراکتر باشد"
    )
    private String name;

    @NotNull(message = "ساعت شروع شیفت را انتخاب کنید")
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @NotNull(message = "ساعت پایان شیفت را انتخاب کنید")
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime endTime;

    @NotNull(message = "مدت تأخیر مجاز را وارد کنید")
    @Min(
            value = 0,
            message = "مدت تأخیر مجاز نمی‌تواند منفی باشد"
    )
    @Max(
            value = 180,
            message = "مدت تأخیر مجاز نمی‌تواند بیشتر از ۱۸۰ دقیقه باشد"
    )
    private Integer lateGraceMinutes = 0;

    @NotNull(message = "مدت خروج زودتر را وارد کنید")
    @Min(
            value = 0,
            message = "مدت خروج زودتر نمی‌تواند منفی باشد"
    )
    @Max(
            value = 180,
            message = "مدت خروج زودتر نمی‌تواند بیشتر از ۱۸۰ دقیقه باشد"
    )
    private Integer earlyLeaveGraceMinutes = 0;

    @NotEmpty(message = "حداقل یک روز کاری را انتخاب کنید")
    private Set<WorkDay> workDays =
            EnumSet.noneOf(WorkDay.class);

    public ShiftForm() {
    }

    public String getShiftCode() {
        return shiftCode;
    }

    public void setShiftCode(String shiftCode) {
        this.shiftCode = shiftCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public Integer getLateGraceMinutes() {
        return lateGraceMinutes;
    }

    public void setLateGraceMinutes(
            Integer lateGraceMinutes
    ) {
        this.lateGraceMinutes = lateGraceMinutes;
    }

    public Integer getEarlyLeaveGraceMinutes() {
        return earlyLeaveGraceMinutes;
    }

    public void setEarlyLeaveGraceMinutes(
            Integer earlyLeaveGraceMinutes
    ) {
        this.earlyLeaveGraceMinutes =
                earlyLeaveGraceMinutes;
    }

    public Set<WorkDay> getWorkDays() {
        return workDays;
    }

    public void setWorkDays(Set<WorkDay> workDays) {
        if (workDays == null || workDays.isEmpty()) {
            this.workDays =
                    EnumSet.noneOf(WorkDay.class);
            return;
        }

        this.workDays = EnumSet.copyOf(workDays);
    }
}