package ir.ac.sutech.attendance_system.service;

import ir.ac.sutech.attendance_system.model.Employee;
import ir.ac.sutech.attendance_system.model.EmployeeShiftAssignment;
import ir.ac.sutech.attendance_system.model.EmployeeType;
import ir.ac.sutech.attendance_system.model.Shift;
import ir.ac.sutech.attendance_system.repository.EmployeeRepository;
import ir.ac.sutech.attendance_system.repository.EmployeeShiftAssignmentRepository;
import ir.ac.sutech.attendance_system.repository.ShiftRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class EmployeeShiftAssignmentService {

    private final EmployeeShiftAssignmentRepository
            assignmentRepository;

    private final EmployeeRepository employeeRepository;

    private final ShiftRepository shiftRepository;

    public EmployeeShiftAssignmentService(
            EmployeeShiftAssignmentRepository
                    assignmentRepository,

            EmployeeRepository employeeRepository,

            ShiftRepository shiftRepository
    ) {
        this.assignmentRepository =
                assignmentRepository;

        this.employeeRepository =
                employeeRepository;

        this.shiftRepository =
                shiftRepository;
    }

    public List<EmployeeShiftAssignment>
    findAllAssignments() {

        return assignmentRepository
                .findAllByOrderByStartDateDescIdDesc();
    }

    public List<EmployeeShiftAssignment>
    findAssignmentsByEmployeeId(
            Long employeeId
    ) {

        return assignmentRepository
                .findByEmployeeIdOrderByStartDateDescIdDesc(
                        employeeId
                );
    }

    public long countAssignments() {
        return assignmentRepository.count();
    }

    /*
     * ثبت شیفت برای یک کارمند.
     *
     * اگر endDate خالی باشد:
     * همان روز به عنوان تاریخ پایان ثبت می‌شود.
     *
     * بنابراین:
     *
     * startDate = 10
     * endDate = null
     *
     * تبدیل می‌شود به:
     *
     * 10 تا 10
     */
    @Transactional
    public EmployeeShiftAssignment assignShift(
            Long employeeId,
            Long shiftId,
            LocalDate startDate,
            LocalDate endDate
    ) {

        if (employeeId == null) {
            throw new IllegalArgumentException(
                    "انتخاب کارمند الزامی است."
            );
        }

        if (shiftId == null) {
            throw new IllegalArgumentException(
                    "انتخاب شیفت الزامی است."
            );
        }

        if (startDate == null) {
            throw new IllegalArgumentException(
                    "تاریخ شروع تخصیص شیفت الزامی است."
            );
        }

        /*
         * خالی بودن تاریخ پایان یعنی فقط همان روز.
         */
        LocalDate effectiveEndDate =
                endDate == null
                        ? startDate
                        : endDate;

        if (effectiveEndDate.isBefore(startDate)) {

            throw new IllegalArgumentException(
                    "تاریخ پایان نمی‌تواند قبل از تاریخ شروع باشد."
            );
        }

        Employee employee =
                employeeRepository
                        .findById(employeeId)
                        .orElseThrow(
                                () ->
                                        new EntityNotFoundException(
                                                "کارمند موردنظر پیدا نشد."
                                        )
                        );

        /*
         * فقط کارکنان خدمات و پشتیبانی
         * باید Assignment شیفت داشته باشند.
         */
        if (
                employee.getEmployeeType()
                        != EmployeeType.SERVICE_SUPPORT
        ) {

            throw new IllegalArgumentException(
                    "اختصاص شیفت فقط برای کارکنان خدمات و پشتیبانی امکان‌پذیر است."
            );
        }

        if (!employee.isActive()) {

            throw new IllegalStateException(
                    "امکان تخصیص شیفت به کارمند غیرفعال وجود ندارد."
            );
        }

        Shift shift =
                shiftRepository
                        .findById(shiftId)
                        .orElseThrow(
                                () ->
                                        new EntityNotFoundException(
                                                "شیفت موردنظر پیدا نشد."
                                        )
                        );

        if (!shift.isActive()) {

            throw new IllegalStateException(
                    "امکان استفاده از شیفت غیرفعال وجود ندارد."
            );
        }

        /*
         * هر کارمند در یک روز فقط یک شیفت دارد.
         *
         * بنابراین هیچ Assignment دیگری نباید
         * با بازه جدید هم‌پوشانی داشته باشد.
         */
        if (
                assignmentRepository
                        .existsOverlappingAssignment(
                                employeeId,
                                startDate,
                                effectiveEndDate
                        )
        ) {

            throw new IllegalArgumentException(
                    "این کارمند در تمام یا بخشی از بازه انتخاب‌شده قبلاً شیفت دارد."
            );
        }

        EmployeeShiftAssignment assignment =
                new EmployeeShiftAssignment(
                        employee,
                        shift,
                        startDate,
                        effectiveEndDate
                );

        return assignmentRepository.save(
                assignment
        );
    }

    /*
     * لغو یک Assignment اشتباه.
     *
     * رکورد حذف نمی‌شود تا سابقه باقی بماند.
     */
    @Transactional
    public void cancelAssignment(
            Long assignmentId
    ) {

        EmployeeShiftAssignment assignment =
                findAssignmentById(
                        assignmentId
                );

        if (!assignment.isActive()) {

            throw new IllegalStateException(
                    "این تخصیص قبلاً لغو شده است."
            );
        }

        assignment.setActive(false);
    }

    public EmployeeShiftAssignment
    findAssignmentById(
            Long assignmentId
    ) {

        return assignmentRepository
                .findById(assignmentId)
                .orElseThrow(
                        () ->
                                new EntityNotFoundException(
                                        "سابقه تخصیص شیفت موردنظر پیدا نشد."
                                )
                );
    }
}