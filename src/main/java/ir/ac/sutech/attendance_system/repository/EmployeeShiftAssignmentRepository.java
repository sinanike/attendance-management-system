package ir.ac.sutech.attendance_system.repository;

import ir.ac.sutech.attendance_system.model.EmployeeShiftAssignment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EmployeeShiftAssignmentRepository
        extends JpaRepository<
        EmployeeShiftAssignment,
        Long
        > {

    @EntityGraph(attributePaths = {
            "employee",
            "employee.department",
            "shift"
    })
    List<EmployeeShiftAssignment>
    findAllByOrderByStartDateDescIdDesc();

    @EntityGraph(attributePaths = {
            "employee",
            "employee.department",
            "shift"
    })
    List<EmployeeShiftAssignment>
    findByEmployeeIdOrderByStartDateDescIdDesc(
            Long employeeId
    );

    /*
     * پیدا کردن Assignment معتبر
     * یک کارمند در یک روز مشخص.
     *
     * این Query مستقیماً توسط
     * DailyAttendanceReportService
     * استفاده می‌شود.
     */
    @EntityGraph(attributePaths = {
            "employee",
            "employee.department",
            "shift"
    })
    @Query("""
            select assignment
            from EmployeeShiftAssignment assignment
            where assignment.employee.id = :employeeId
              and assignment.active = true
              and assignment.startDate <= :reportDate
              and (
                    assignment.endDate is null
                    or assignment.endDate >= :reportDate
                  )
            order by assignment.startDate desc,
                     assignment.id desc
            """)
    List<EmployeeShiftAssignment>
    findEffectiveAssignments(
            @Param("employeeId")
            Long employeeId,

            @Param("reportDate")
            LocalDate reportDate
    );

    default Optional<EmployeeShiftAssignment>
    findEffectiveAssignment(
            Long employeeId,
            LocalDate reportDate
    ) {

        List<EmployeeShiftAssignment> assignments =
                findEffectiveAssignments(
                        employeeId,
                        reportDate
                );

        if (assignments.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(
                assignments.get(0)
        );
    }

    /*
     * مهم:
     *
     * بررسی هم‌پوشانی دو بازه شیفت.
     *
     * مثال:
     *
     * قبلی:
     * 10 تا 15 مرداد
     *
     * جدید:
     * 13 تا 17 مرداد
     *
     * باید تداخل تشخیص داده شود.
     */
    @Query("""
            select count(assignment) > 0
            from EmployeeShiftAssignment assignment
            where assignment.employee.id = :employeeId
              and assignment.active = true
              and assignment.startDate <= :endDate
              and (
                    assignment.endDate is null
                    or assignment.endDate >= :startDate
                  )
            """)
    boolean existsOverlappingAssignment(
            @Param("employeeId")
            Long employeeId,

            @Param("startDate")
            LocalDate startDate,

            @Param("endDate")
            LocalDate endDate
    );

    /*
     * Assignmentهای مؤثر داخل یک بازه.
     *
     * بعداً برای تقویم ماهانه شیفت‌ها
     * هم از این متد استفاده می‌کنیم.
     */
    @EntityGraph(attributePaths = {
            "employee",
            "employee.department",
            "shift"
    })
    @Query("""
            select assignment
            from EmployeeShiftAssignment assignment
            where assignment.active = true
              and assignment.startDate <= :toDate
              and (
                    assignment.endDate is null
                    or assignment.endDate >= :fromDate
                  )
            order by assignment.startDate asc,
                     assignment.employee.lastName asc,
                     assignment.employee.firstName asc
            """)
    List<EmployeeShiftAssignment>
    findEffectiveAssignmentsBetween(
            @Param("fromDate")
            LocalDate fromDate,

            @Param("toDate")
            LocalDate toDate
    );
}