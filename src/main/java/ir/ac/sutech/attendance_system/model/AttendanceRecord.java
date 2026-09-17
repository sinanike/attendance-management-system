package ir.ac.sutech.attendance_system.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "attendance_records",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_attendance_device_record",
                        columnNames = {
                                "device_code",
                                "device_record_id"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_attendance_employee_time",
                        columnList = "employee_id,event_time"
                )
        }
)
public class AttendanceRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "employee_id",
            nullable = false
    )
    private Employee employee;

    @Column(
            name = "event_time",
            nullable = false
    )
    private LocalDateTime eventTime;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "attendance_type",
            nullable = false,
            length = 20
    )
    private AttendanceType attendanceType;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "attendance_source",
            nullable = false,
            length = 20
    )
    private AttendanceSource attendanceSource;

    @Column(
            name = "device_code",
            length = 50
    )
    private String deviceCode;

    @Column(
            name = "device_record_id",
            length = 100
    )
    private String deviceRecordId;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    public AttendanceRecord() {
    }

    @PrePersist
    public void beforeInsert() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }

        if (attendanceSource == null) {
            attendanceSource = AttendanceSource.MANUAL;
        }
    }

    public Long getId() {
        return id;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
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

    public AttendanceSource getAttendanceSource() {
        return attendanceSource;
    }

    public void setAttendanceSource(
            AttendanceSource attendanceSource
    ) {
        this.attendanceSource = attendanceSource;
    }

    public String getDeviceCode() {
        return deviceCode;
    }

    public void setDeviceCode(String deviceCode) {
        this.deviceCode = deviceCode;
    }

    public String getDeviceRecordId() {
        return deviceRecordId;
    }

    public void setDeviceRecordId(String deviceRecordId) {
        this.deviceRecordId = deviceRecordId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}