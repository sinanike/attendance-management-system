package ir.ac.sutech.attendance_system.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "attendance_rule_settings")
public class AttendanceRuleSettings {

    public static final Long SINGLETON_ID = 1L;

    @Id
    private Long id = SINGLETON_ID;

    @Column(name = "annual_leave_days", nullable = false)
    private int annualLeaveDays = 30;

    @Column(name = "minimum_attendance_percent", nullable = false)
    private double minimumAttendancePercent = 80.0;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected AttendanceRuleSettings() {
    }

    public static AttendanceRuleSettings defaultSettings() {
        return new AttendanceRuleSettings();
    }

    @PrePersist
    @PreUpdate
    public void touch() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public int getAnnualLeaveDays() {
        return annualLeaveDays;
    }

    public void setAnnualLeaveDays(int annualLeaveDays) {
        this.annualLeaveDays = annualLeaveDays;
    }

    public double getMinimumAttendancePercent() {
        return minimumAttendancePercent;
    }

    public void setMinimumAttendancePercent(double minimumAttendancePercent) {
        this.minimumAttendancePercent = minimumAttendancePercent;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
