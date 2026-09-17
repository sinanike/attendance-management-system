package ir.ac.sutech.attendance_system.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "mission_requests",
        indexes = {
                @Index(
                        name = "idx_mission_employee",
                        columnList = "employee_id"
                ),
                @Index(
                        name = "idx_mission_status",
                        columnList = "status"
                ),
                @Index(
                        name = "idx_mission_dates",
                        columnList = "start_date,end_date"
                ),
                @Index(
                        name = "idx_mission_employee_dates",
                        columnList = "employee_id,start_date,end_date"
                )
        }
)
public class MissionRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "employee_id",
            nullable = false,
            foreignKey =
            @ForeignKey(
                    name = "fk_mission_employee"
            )
    )
    private Employee employee;


    @Column(
            name = "start_date",
            nullable = false
    )
    private LocalDate startDate;


    @Column(
            name = "end_date",
            nullable = false
    )
    private LocalDate endDate;


    @Column(
            length = 300
    )
    private String destination;


    @Column(
            length = 1000
    )
    private String purpose;


    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 30
    )
    private MissionStatus status =
            MissionStatus.PENDING;


    @Column(
            name = "review_note",
            length = 1000
    )
    private String reviewNote;


    @Column(
            name = "created_at",
            nullable = false
    )
    private LocalDateTime createdAt;


    @Column(
            name = "updated_at",
            nullable = false
    )
    private LocalDateTime updatedAt;


    @Column(
            name = "reviewed_at"
    )
    private LocalDateTime reviewedAt;


    protected MissionRequest() {
    }


    public MissionRequest(
            Employee employee,
            LocalDate startDate,
            LocalDate endDate
    ) {
        this.employee = employee;
        this.startDate = startDate;
        this.endDate = endDate;
    }


    @PrePersist
    public void onCreate() {

        LocalDateTime now =
                LocalDateTime.now();

        createdAt = now;
        updatedAt = now;

        if (status == null) {
            status = MissionStatus.PENDING;
        }
    }


    @PreUpdate
    public void onUpdate() {
        updatedAt =
                LocalDateTime.now();
    }


    public void approve(
            String reviewNote
    ) {

        this.status =
                MissionStatus.APPROVED;

        this.reviewNote =
                reviewNote;

        this.reviewedAt =
                LocalDateTime.now();
    }


    public void reject(
            String reviewNote
    ) {

        this.status =
                MissionStatus.REJECTED;

        this.reviewNote =
                reviewNote;

        this.reviewedAt =
                LocalDateTime.now();
    }


    public void markPending() {

        this.status =
                MissionStatus.PENDING;

        this.reviewNote = null;
        this.reviewedAt = null;
    }


    public boolean coversDate(
            LocalDate date
    ) {

        if (
                date == null
                        || startDate == null
                        || endDate == null
        ) {
            return false;
        }

        return !date.isBefore(startDate)
                && !date.isAfter(endDate);
    }


    public boolean isPending() {
        return status
                == MissionStatus.PENDING;
    }


    public boolean isApproved() {
        return status
                == MissionStatus.APPROVED;
    }


    public boolean isRejected() {
        return status
                == MissionStatus.REJECTED;
    }


    public Long getId() {
        return id;
    }


    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(
            Employee employee
    ) {
        this.employee = employee;
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


    public MissionStatus getStatus() {
        return status;
    }

    public void setStatus(
            MissionStatus status
    ) {
        this.status = status;
    }


    public String getReviewNote() {
        return reviewNote;
    }


    public LocalDateTime getCreatedAt() {
        return createdAt;
    }


    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }


    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }
}