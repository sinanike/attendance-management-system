package ir.ac.sutech.attendance_system.dto;

import ir.ac.sutech.attendance_system.model.EmployeeType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class EmployeeForm {

    @NotBlank(message = "کد پرسنلی را وارد کنید")
    @Size(
            max = 30,
            message = "کد پرسنلی نباید بیشتر از ۳۰ کاراکتر باشد"
    )
    @Pattern(
            regexp = "[A-Za-z0-9-]+",
            message = "کد پرسنلی فقط می‌تواند شامل حروف انگلیسی، عدد و خط تیره باشد"
    )
    private String personnelCode;

    /*
     * شناسه کارمند داخل دستگاه اثر انگشت
     * این فیلد اختیاری است.
     */
    @Size(
            max = 50,
            message = "شناسه دستگاه نباید بیشتر از ۵۰ کاراکتر باشد"
    )
    @Pattern(
            regexp = "[A-Za-z0-9_-]*",
            message = "شناسه دستگاه فقط می‌تواند شامل حروف انگلیسی، عدد، خط تیره و زیرخط باشد"
    )
    private String deviceUserId;

    @NotBlank(message = "کد ملی را وارد کنید")
    @Pattern(
            regexp = "\\d{10}",
            message = "کد ملی باید دقیقاً ۱۰ رقم باشد"
    )
    private String nationalCode;

    @NotBlank(message = "نام را وارد کنید")
    @Size(
            max = 50,
            message = "نام نباید بیشتر از ۵۰ کاراکتر باشد"
    )
    private String firstName;

    @NotBlank(message = "نام خانوادگی را وارد کنید")
    @Size(
            max = 70,
            message = "نام خانوادگی نباید بیشتر از ۷۰ کاراکتر باشد"
    )
    private String lastName;

    @NotNull(message = "نوع نیرو را انتخاب کنید")
    private EmployeeType employeeType;

    @NotNull(message = "واحد سازمانی را انتخاب کنید")
    private Long departmentId;

    private Long shiftId;

    public String getPersonnelCode() {
        return personnelCode;
    }

    public void setPersonnelCode(String personnelCode) {
        this.personnelCode = personnelCode;
    }

    public String getDeviceUserId() {
        return deviceUserId;
    }

    public void setDeviceUserId(String deviceUserId) {
        this.deviceUserId = deviceUserId;
    }

    public String getNationalCode() {
        return nationalCode;
    }

    public void setNationalCode(String nationalCode) {
        this.nationalCode = nationalCode;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public EmployeeType getEmployeeType() {
        return employeeType;
    }

    public void setEmployeeType(EmployeeType employeeType) {
        this.employeeType = employeeType;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public Long getShiftId() {
        return shiftId;
    }

    public void setShiftId(Long shiftId) {
        this.shiftId = shiftId;
    }
}