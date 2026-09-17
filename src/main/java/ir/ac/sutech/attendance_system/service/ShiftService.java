package ir.ac.sutech.attendance_system.service;

import ir.ac.sutech.attendance_system.dto.ShiftForm;
import ir.ac.sutech.attendance_system.model.Shift;
import ir.ac.sutech.attendance_system.repository.ShiftRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ShiftService {

    private final ShiftRepository shiftRepository;

    public ShiftService(
            ShiftRepository shiftRepository
    ) {
        this.shiftRepository = shiftRepository;
    }

    public List<Shift> findAllShifts() {
        return shiftRepository
                .findAllByOrderByStartTimeAsc();
    }

    public List<Shift> findActiveShifts() {
        return shiftRepository
                .findAllByActiveTrueOrderByNameAsc();
    }

    public long countShifts() {
        return shiftRepository.count();
    }

    public boolean shiftCodeExists(
            String shiftCode
    ) {
        if (shiftCode == null || shiftCode.isBlank()) {
            return false;
        }

        return shiftRepository
                .existsByShiftCodeIgnoreCase(
                        shiftCode.trim()
                );
    }

    public boolean shiftCodeExistsForAnotherShift(
            String shiftCode,
            Long shiftId
    ) {
        if (shiftCode == null || shiftCode.isBlank()) {
            return false;
        }

        return shiftRepository
                .existsByShiftCodeIgnoreCaseAndIdNot(
                        shiftCode.trim(),
                        shiftId
                );
    }

    @Transactional
    public Shift createShift(
            ShiftForm shiftForm
    ) {
        Shift shift = new Shift(
                normalizeShiftCode(
                        shiftForm.getShiftCode()
                ),
                shiftForm.getName().trim(),
                shiftForm.getStartTime(),
                shiftForm.getEndTime(),
                shiftForm.getLateGraceMinutes(),
                shiftForm.getEarlyLeaveGraceMinutes()
        );

        shift.setWorkDays(
                shiftForm.getWorkDays()
        );

        return shiftRepository.save(shift);
    }

    public ShiftForm findShiftFormById(
            Long shiftId
    ) {
        Shift shift = findShiftById(shiftId);

        ShiftForm shiftForm = new ShiftForm();

        shiftForm.setShiftCode(
                shift.getShiftCode()
        );

        shiftForm.setName(
                shift.getName()
        );

        shiftForm.setStartTime(
                shift.getStartTime()
        );

        shiftForm.setEndTime(
                shift.getEndTime()
        );

        shiftForm.setLateGraceMinutes(
                shift.getLateGraceMinutes()
        );

        shiftForm.setEarlyLeaveGraceMinutes(
                shift.getEarlyLeaveGraceMinutes()
        );

        shiftForm.setWorkDays(
                shift.getWorkDays()
        );

        return shiftForm;
    }

    @Transactional
    public void updateShift(
            Long shiftId,
            ShiftForm shiftForm
    ) {
        Shift shift = findShiftById(shiftId);

        shift.setShiftCode(
                normalizeShiftCode(
                        shiftForm.getShiftCode()
                )
        );

        shift.setName(
                shiftForm.getName().trim()
        );

        shift.setStartTime(
                shiftForm.getStartTime()
        );

        shift.setEndTime(
                shiftForm.getEndTime()
        );

        shift.setLateGraceMinutes(
                shiftForm.getLateGraceMinutes()
        );

        shift.setEarlyLeaveGraceMinutes(
                shiftForm.getEarlyLeaveGraceMinutes()
        );

        shift.setWorkDays(
                shiftForm.getWorkDays()
        );
    }

    @Transactional
    public void updateShiftStatus(
            Long shiftId,
            boolean active
    ) {
        Shift shift = findShiftById(shiftId);

        shift.setActive(active);
    }

    public Shift findShiftById(
            Long shiftId
    ) {
        return shiftRepository
                .findById(shiftId)
                .orElseThrow(
                        () -> new EntityNotFoundException(
                                "شیفت موردنظر پیدا نشد"
                        )
                );
    }

    private String normalizeShiftCode(
            String shiftCode
    ) {
        return shiftCode
                .trim()
                .toUpperCase();
    }
}