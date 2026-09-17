package ir.ac.sutech.attendance_system.repository;

import ir.ac.sutech.attendance_system.model.ShiftDaySchedule;
import ir.ac.sutech.attendance_system.model.WorkDay;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShiftDayScheduleRepository
        extends JpaRepository<ShiftDaySchedule, Long> {

    @EntityGraph(attributePaths = "shift")
    List<ShiftDaySchedule> findByShiftId(Long shiftId);

    Optional<ShiftDaySchedule> findByShiftIdAndWorkDay(
            Long shiftId,
            WorkDay workDay
    );
}