package ir.ac.sutech.attendance_system.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "attendance_settings")
public class AttendanceSettings {

    public static final Long SINGLETON_ID = 1L;

    @Id
    private Long id = SINGLETON_ID;

    @Column(
            name = "manual_correction_enabled",
            nullable = false
    )
    private boolean manualCorrectionEnabled = true;

    @Column(
            name = "max_correction_days",
            nullable = false
    )
    private int maxCorrectionDays = 30;

    @Column(
            name = "allow_manual_record_deletion",
            nullable = false
    )
    private boolean allowManualRecordDeletion = true;

    @Column(
            name = "operator_note",
            length = 500
    )
    private String note;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private LocalDateTime updatedAt;

    public AttendanceSettings() {
    }

    @PrePersist
    @PreUpdate
    public void touch() {

        updatedAt =
                LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public boolean isManualCorrectionEnabled() {
        return manualCorrectionEnabled;
    }

    public void setManualCorrectionEnabled(
            boolean manualCorrectionEnabled
    ) {
        this.manualCorrectionEnabled =
                manualCorrectionEnabled;
    }

    public int getMaxCorrectionDays() {
        return maxCorrectionDays;
    }

    public void setMaxCorrectionDays(
            int maxCorrectionDays
    ) {
        this.maxCorrectionDays =
                maxCorrectionDays;
    }

    public boolean isAllowManualRecordDeletion() {
        return allowManualRecordDeletion;
    }

    public void setAllowManualRecordDeletion(
            boolean allowManualRecordDeletion
    ) {
        this.allowManualRecordDeletion =
                allowManualRecordDeletion;
    }

    public String getNote() {
        return note;
    }

    public void setNote(
            String note
    ) {
        this.note = note;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}