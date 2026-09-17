package ir.ac.sutech.attendance_system.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(
        name = "leave_requests",
        indexes = {
                @Index(
                        name = "idx_leave_employee",
                        columnList = "employee_id"
                ),
                @Index(
                        name = "idx_leave_status",
                        columnList = "status"
                ),
                @Index(
                        name = "idx_leave_dates",
                        columnList = "start_date,end_date"
                ),
                @Index(
                        name = "idx_leave_employee_dates",
                        columnList = "employee_id,start_date,end_date"
                )
        }
)
public class LeaveRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * کارمندی که درخواست مرخصی برای او ثبت شده است.
     */
    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "employee_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_leave_request_employee"
            )
    )
    private Employee employee;

    /*
     * نوع مرخصی
     */
    @Enumerated(EnumType.STRING)
    @Column(
            name = "leave_type",
            nullable = false,
            length = 30
    )
    private LeaveType leaveType;

    /*
     * وضعیت بررسی درخواست
     */
    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 30
    )
    private LeaveStatus status =
            LeaveStatus.PENDING;

    /*
     * تاریخ شروع مرخصی
     */
    @Column(
            name = "start_date",
            nullable = false
    )
    private LocalDate startDate;

    /*
     * تاریخ پایان مرخصی
     *
     * برای مرخصی یک‌روزه:
     * startDate و endDate برابر هستند.
     */
    @Column(
            name = "end_date",
            nullable = false
    )
    private LocalDate endDate;

    /*
     * فقط برای مرخصی ساعتی استفاده می‌شوند.
     *
     * برای مرخصی روزانه null خواهند بود.
     */
    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    /*
     * توضیح یا دلیل درخواست
     */
    @Column(
            name = "reason",
            length = 1000
    )
    private String reason;

    /*
     * توضیح مدیر هنگام بررسی درخواست.
     *
     * مثلاً:
     * "با توجه به کمبود نیرو رد شد."
     */
    @Column(
            name = "review_note",
            length = 1000
    )
    private String reviewNote;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private LocalDateTime updatedAt;

    /*
     * زمان تأیید یا رد درخواست
     */
    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    protected LeaveRequest() {
    }

    public LeaveRequest(
            Employee employee,
            LeaveType leaveType,
            LocalDate startDate,
            LocalDate endDate
    ) {
        setEmployee(employee);
        setLeaveType(leaveType);
        setStartDate(startDate);
        setEndDate(endDate);
    }

    @PrePersist
    public void beforeInsert() {

        LocalDateTime now =
                LocalDateTime.now();

        if (createdAt == null) {
            createdAt = now;
        }

        updatedAt = now;

        if (status == null) {
            status = LeaveStatus.PENDING;
        }
    }

    @PreUpdate
    public void beforeUpdate() {
        updatedAt = LocalDateTime.now();
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
        if (employee == null) {
            throw new IllegalArgumentException(
                    "انتخاب کارمند برای مرخصی الزامی است."
            );
        }

        this.employee = employee;
    }

    public LeaveType getLeaveType() {
        return leaveType;
    }

    public void setLeaveType(
            LeaveType leaveType
    ) {
        if (leaveType == null) {
            throw new IllegalArgumentException(
                    "نوع مرخصی الزامی است."
            );
        }

        this.leaveType = leaveType;
    }

    public LeaveStatus getStatus() {
        return status;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(
            LocalDate startDate
    ) {
        if (startDate == null) {
            throw new IllegalArgumentException(
                    "تاریخ شروع مرخصی الزامی است."
            );
        }

        if (
                endDate != null
                        && endDate.isBefore(startDate)
        ) {
            throw new IllegalArgumentException(
                    "تاریخ شروع نمی‌تواند بعد از تاریخ پایان باشد."
            );
        }

        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(
            LocalDate endDate
    ) {
        if (endDate == null) {
            throw new IllegalArgumentException(
                    "تاریخ پایان مرخصی الزامی است."
            );
        }

        if (
                startDate != null
                        && endDate.isBefore(startDate)
        ) {
            throw new IllegalArgumentException(
                    "تاریخ پایان نمی‌تواند قبل از تاریخ شروع باشد."
            );
        }

        this.endDate = endDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(
            LocalTime startTime
    ) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(
            LocalTime endTime
    ) {
        this.endTime = endTime;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(
            String reason
    ) {
        if (reason == null || reason.isBlank()) {
            this.reason = null;
            return;
        }

        this.reason = reason.trim();
    }

    public String getReviewNote() {
        return reviewNote;
    }

    public void setReviewNote(
            String reviewNote
    ) {
        if (
                reviewNote == null
                        || reviewNote.isBlank()
        ) {
            this.reviewNote = null;
            return;
        }

        this.reviewNote =
                reviewNote.trim();
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

    /*
     * تأیید درخواست
     */
    public void approve(
            String reviewNote
    ) {

        if (status == LeaveStatus.APPROVED) {
            throw new IllegalStateException(
                    "این درخواست قبلاً تأیید شده است."
            );
        }

        status =
                LeaveStatus.APPROVED;

        setReviewNote(reviewNote);

        reviewedAt =
                LocalDateTime.now();
    }

    /*
     * رد درخواست
     */
    public void reject(
            String reviewNote
    ) {

        if (status == LeaveStatus.REJECTED) {
            throw new IllegalStateException(
                    "این درخواست قبلاً رد شده است."
            );
        }

        status =
                LeaveStatus.REJECTED;

        setReviewNote(reviewNote);

        reviewedAt =
                LocalDateTime.now();
    }

    /*
     * بازگرداندن درخواست به حالت انتظار.
     *
     * بعداً برای اصلاح تصمیم مدیر مفید است.
     */
    public void markPending() {

        status =
                LeaveStatus.PENDING;

        reviewedAt = null;
        reviewNote = null;
    }

    /*
     * آیا مرخصی این تاریخ را پوشش می‌دهد؟
     */
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

    public boolean isApproved() {
        return status == LeaveStatus.APPROVED;
    }

    public boolean isPending() {
        return status == LeaveStatus.PENDING;
    }

    public boolean isRejected() {
        return status == LeaveStatus.REJECTED;
    }

    public boolean isHourly() {
        return leaveType == LeaveType.HOURLY;
    }

    public boolean isDaily() {
        return leaveType == LeaveType.DAILY;
    }

    public boolean isSickLeave() {
        return leaveType == LeaveType.SICK;
    }
}