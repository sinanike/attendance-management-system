package ir.ac.sutech.attendance_system.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "performance_tasks")
public class PerformanceTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "review_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_performance_task_review")
    )
    private QuarterlyPerformanceReview review;

    @Column(nullable = false, length = 250)
    private String title;

    @Column(length = 1200)
    private String description;

    @Column(name = "weight_percent", nullable = false)
    private int weightPercent;

    @Column(name = "completion_percent", nullable = false)
    private int completionPercent;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected PerformanceTask() {
    }

    public PerformanceTask(
            QuarterlyPerformanceReview review,
            String title,
            String description,
            int weightPercent
    ) {
        this.review = review;
        this.title = title;
        this.description = description;
        this.weightPercent = weightPercent;
        this.completionPercent = 0;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public QuarterlyPerformanceReview getReview() {
        return review;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getWeightPercent() {
        return weightPercent;
    }

    public int getCompletionPercent() {
        return completionPercent;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCompletionPercent(int completionPercent) {
        this.completionPercent = completionPercent;
    }
}
