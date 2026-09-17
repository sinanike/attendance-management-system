package ir.ac.sutech.attendance_system.dto;

import ir.ac.sutech.attendance_system.model.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class UserAccountForm {

    @NotBlank(message = "نام کاربری را وارد کنید")
    @Size(
            min = 4,
            max = 50,
            message = "نام کاربری باید بین ۴ تا ۵۰ کاراکتر باشد"
    )
    @Pattern(
            regexp = "[A-Za-z0-9._-]+",
            message = "نام کاربری فقط می‌تواند شامل حروف انگلیسی، عدد، نقطه، خط تیره و زیرخط باشد"
    )
    private String username;

    private String password;

    @NotNull(message = "سطح دسترسی را انتخاب کنید")
    private Role role;

    private Long employeeId;

    private boolean active = true;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}