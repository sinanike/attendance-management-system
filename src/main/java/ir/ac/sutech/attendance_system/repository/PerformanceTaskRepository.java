package ir.ac.sutech.attendance_system.repository;

import ir.ac.sutech.attendance_system.model.PerformanceTask;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PerformanceTaskRepository
        extends JpaRepository<PerformanceTask, Long> {

    @Override
    @EntityGraph(attributePaths = {
            "review",
            "review.employee",
            "review.employee.department",
            "review.reviewer"
    })
    Optional<PerformanceTask> findById(Long id);
}
