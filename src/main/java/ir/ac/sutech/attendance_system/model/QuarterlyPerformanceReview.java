package ir.ac.sutech.attendance_system.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "quarterly_performance_reviews",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_qpr_employee_year_quarter",
                columnNames = {"employee_id", "review_year", "quarter_no"}
        )
)
public class QuarterlyPerformanceReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "employee_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_qpr_employee")
    )
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "reviewer_employee_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_qpr_reviewer_employee")
    )
    private Employee reviewer;

    @Column(name = "review_year", nullable = false)
    private int year;

    @Column(name = "quarter_no", nullable = false)
    private int quarter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PerformanceReviewStatus status = PerformanceReviewStatus.DRAFT;

    @Column(name = "manager_note", length = 1500)
    private String managerNote;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @OneToMany(
            mappedBy = "review",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("id ASC")
    private List<PerformanceTask> tasks = new ArrayList<>();

    protected QuarterlyPerformanceReview() {
    }

    public QuarterlyPerformanceReview(
            Employee employee,
            Employee reviewer,
            int year,
            int quarter
    ) {
        this.employee = employee;
        this.reviewer = reviewer;
        this.year = year;
        this.quarter = quarter;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Employee getEmployee() {
        return employee;
    }

    public Employee getReviewer() {
        return reviewer;
    }

    public int getYear() {
        return year;
    }

    public int getQuarter() {
        return quarter;
    }

    public PerformanceReviewStatus getStatus() {
        return status;
    }

    public String getManagerNote() {
        return managerNote;
    }

    public void setManagerNote(String managerNote) {
        this.managerNote = managerNote;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public List<PerformanceTask> getTasks() {
        return tasks;
    }

    public boolean isDraft() {
        return status == PerformanceReviewStatus.DRAFT;
    }

    public boolean isSubmitted() {
        return status == PerformanceReviewStatus.SUBMITTED;
    }

    public void submit(String managerNote) {
        this.managerNote = managerNote;
        this.status = PerformanceReviewStatus.SUBMITTED;
        this.submittedAt = LocalDateTime.now();
    }
}
