package ir.ac.sutech.attendance_system.repository;

import ir.ac.sutech.attendance_system.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DepartmentRepository
        extends JpaRepository<Department, Long> {

    List<Department> findAllByActiveTrueOrderByNameAsc();
}