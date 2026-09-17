package ir.ac.sutech.attendance_system.repository;

import ir.ac.sutech.attendance_system.model.Shift;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShiftRepository
        extends JpaRepository<Shift, Long> {

    List<Shift> findAllByOrderByStartTimeAsc();

    List<Shift> findAllByActiveTrueOrderByNameAsc();

    boolean existsByShiftCodeIgnoreCase(
            String shiftCode
    );

    boolean existsByShiftCodeIgnoreCaseAndIdNot(
            String shiftCode,
            Long shiftId
    );
}