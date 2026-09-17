package ir.ac.sutech.attendance_system.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.time.LocalTime;
import java.util.EnumSet;
import java.util.Set;

@Entity
@Table(
        name = "shifts",
        indexes = {
                @Index(
                        name = "idx_shifts_active",
                        columnList = "active"
                ),
                @Index(
                        name = "idx_shifts_code",
                        columnList = "shift_code"
                )
        }
)
public class Shift {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "shift_code",
            nullable = false,
            unique = true,
            length = 20
    )
    private String shiftCode;

    @Column(
            name = "name",
            nullable = false,
            length = 70
    )
    private String name;

    @Column(
            name = "start_time",
            nullable = false
    )
    private LocalTime startTime;

    @Column(
            name = "end_time",
            nullable = false
    )
    private LocalTime endTime;

    @Column(
            name = "late_grace_minutes",
            nullable = false
    )
    private int lateGraceMinutes = 0;

    @Column(
            name = "early_leave_grace_minutes",
            nullable = false
    )
    private int earlyLeaveGraceMinutes = 0;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "shift_work_days",
            joinColumns = @JoinColumn(name = "shift_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(
            name = "work_day",
            nullable = false,
            length = 20
    )
    private Set<WorkDay> workDays =
            EnumSet.noneOf(WorkDay.class);

    @Column(nullable = false)
    private boolean active = true;

    protected Shift() {
    }

    public Shift(
            String shiftCode,
            String name,
            LocalTime startTime,
            LocalTime endTime,
            int lateGraceMinutes,
            int earlyLeaveGraceMinutes
    ) {
        this.shiftCode = shiftCode;
        this.name = name;
        this.startTime = startTime;
        this.endTime = endTime;
        this.lateGraceMinutes = lateGraceMinutes;
        this.earlyLeaveGraceMinutes =
                earlyLeaveGraceMinutes;
    }

    public Long getId() {
        return id;
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

    public int getLateGraceMinutes() {
        return lateGraceMinutes;
    }

    public void setLateGraceMinutes(
            int lateGraceMinutes
    ) {
        this.lateGraceMinutes = lateGraceMinutes;
    }

    public int getEarlyLeaveGraceMinutes() {
        return earlyLeaveGraceMinutes;
    }

    public void setEarlyLeaveGraceMinutes(
            int earlyLeaveGraceMinutes
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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean crossesMidnight() {
        return !endTime.isAfter(startTime);
    }
}