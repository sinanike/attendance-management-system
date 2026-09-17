package ir.ac.sutech.attendance_system.repository;

import ir.ac.sutech.attendance_system.model.PerformanceReviewStatus;
import ir.ac.sutech.attendance_system.model.QuarterlyPerformanceReview;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuarterlyPerformanceReviewRepository
        extends JpaRepository<QuarterlyPerformanceReview, Long> {

    @EntityGraph(attributePaths = {
            "employee",
            "employee.department",
            "reviewer",
            "tasks"
    })
    Optional<QuarterlyPerformanceReview>
    findByEmployeeIdAndYearAndQuarter(
            Long employeeId,
            int year,
            int quarter
    );

    @EntityGraph(attributePaths = {
            "employee",
            "employee.department",
            "reviewer",
            "tasks"
    })
    List<QuarterlyPerformanceReview>
    findByStatusAndYearAndQuarterOrderByEmployeeLastNameAscEmployeeFirstNameAsc(
            PerformanceReviewStatus status,
            int year,
            int quarter
    );
}
