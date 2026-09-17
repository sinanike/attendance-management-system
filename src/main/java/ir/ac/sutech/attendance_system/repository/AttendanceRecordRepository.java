package ir.ac.sutech.attendance_system.repository;

import ir.ac.sutech.attendance_system.model.AttendanceRecord;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AttendanceRecordRepository
        extends JpaRepository<AttendanceRecord, Long> {

    @EntityGraph(attributePaths = {
            "employee",
            "employee.department",
            "employee.shift"
    })
    List<AttendanceRecord>
    findByEmployee_IdAndEventTimeBetweenOrderByEventTimeDesc(
            Long employeeId,
            LocalDateTime startTime,
            LocalDateTime endTime
    );

    @EntityGraph(attributePaths = {
            "employee",
            "employee.department",
            "employee.shift"
    })
    List<AttendanceRecord>
    findAllByOrderByEventTimeDesc();

    @EntityGraph(attributePaths = {
            "employee",
            "employee.department",
            "employee.shift"
    })
    List<AttendanceRecord>
    findByEventTimeBetweenOrderByEventTimeDesc(
            LocalDateTime startTime,
            LocalDateTime endTime
    );

    @EntityGraph(attributePaths = {
            "employee",
            "employee.department",
            "employee.shift"
    })
    List<AttendanceRecord>
    findByEventTimeGreaterThanEqualAndEventTimeLessThanOrderByEventTimeDesc(
            LocalDateTime startTime,
            LocalDateTime endTime
    );

    @EntityGraph(attributePaths = {
            "employee",
            "employee.department",
            "employee.shift"
    })
    List<AttendanceRecord>
    findByEmployeeIdAndEventTimeGreaterThanEqualAndEventTimeLessThanOrderByEventTimeDesc(
            Long employeeId,
            LocalDateTime startTime,
            LocalDateTime endTime
    );

    boolean existsByDeviceCodeAndDeviceRecordId(
            String deviceCode,
            String deviceRecordId
    );
}