package ir.ac.sutech.attendance_system.dto;

import ir.ac.sutech.attendance_system.model.QuarterlyPerformanceReview;

public class QuarterlyPerformanceRow {

    private final QuarterlyPerformanceReview review;
    private final double taskScore;
    private final double attendanceRate;
    private final double productivityScore;
    private final long presentDays;
    private final long absentDays;
    private final long incompleteDays;

    public QuarterlyPerformanceRow(
            QuarterlyPerformanceReview review,
            double taskScore,
            double attendanceRate,
            double productivityScore,
            long presentDays,
            long absentDays,
            long incompleteDays
    ) {
        this.review = review;
        this.taskScore = taskScore;
        this.attendanceRate = attendanceRate;
        this.productivityScore = productivityScore;
        this.presentDays = presentDays;
        this.absentDays = absentDays;
        this.incompleteDays = incompleteDays;
    }

    public QuarterlyPerformanceReview getReview() {
        return review;
    }

    public double getTaskScore() {
        return taskScore;
    }

    public double getAttendanceRate() {
        return attendanceRate;
    }

    public double getProductivityScore() {
        return productivityScore;
    }

    public long getPresentDays() {
        return presentDays;
    }

    public long getAbsentDays() {
        return absentDays;
    }

    public long getIncompleteDays() {
        return incompleteDays;
    }
}
