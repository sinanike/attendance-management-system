package ir.ac.sutech.attendance_system.repository;

import ir.ac.sutech.attendance_system.model.LeaveRequest;
import ir.ac.sutech.attendance_system.model.LeaveStatus;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

import java.util.List;
import java.util.Optional;


public interface LeaveRequestRepository
        extends JpaRepository<LeaveRequest, Long> {


    /*
     * =====================================================
     * تمام درخواست‌ها
     * ADMIN
     * =====================================================
     */

    @EntityGraph(attributePaths = {
            "employee",
            "employee.department"
    })
    List<LeaveRequest>
    findAllByOrderByCreatedAtDescIdDesc();


    /*
     * =====================================================
     * درخواست‌های یک Employee
     * =====================================================
     */

    @EntityGraph(attributePaths = {
            "employee",
            "employee.department"
    })
    List<LeaveRequest>
    findByEmployeeIdOrderByCreatedAtDescIdDesc(
            Long employeeId
    );


    /*
     * =====================================================
     * دریافت امن یک درخواست برای Employee
     * =====================================================
     */

    @EntityGraph(attributePaths = {
            "employee",
            "employee.department"
    })
    Optional<LeaveRequest>
    findByIdAndEmployeeId(
            Long id,
            Long employeeId
    );


    /*
     * =====================================================
     * درخواست‌های Admin براساس Status
     * =====================================================
     */

    @EntityGraph(attributePaths = {
            "employee",
            "employee.department"
    })
    List<LeaveRequest>
    findByStatusOrderByCreatedAtDescIdDesc(
            LeaveStatus status
    );


    /*
     * =====================================================
     * درخواست‌های یک Department
     * =====================================================
     */

    @EntityGraph(attributePaths = {
            "employee",
            "employee.department"
    })
    List<LeaveRequest>
    findByEmployeeDepartmentIdOrderByCreatedAtDescIdDesc(
            Long departmentId
    );


    /*
     * =====================================================
     * درخواست‌های یک Department براساس Status
     * =====================================================
     */

    @EntityGraph(attributePaths = {
            "employee",
            "employee.department"
    })
    List<LeaveRequest>
    findByEmployeeDepartmentIdAndStatusOrderByCreatedAtDescIdDesc(
            Long departmentId,
            LeaveStatus status
    );


    /*
     * =====================================================
     * دریافت امن درخواست توسط Manager
     *
     * هم ID و هم Department بررسی می‌شوند.
     * =====================================================
     */

    @EntityGraph(attributePaths = {
            "employee",
            "employee.department"
    })
    Optional<LeaveRequest>
    findByIdAndEmployeeDepartmentId(
            Long id,
            Long departmentId
    );


    /*
     * =====================================================
     * تعداد درخواست‌های Pending واحد مدیر
     *
     * درخواست خود مدیر محاسبه نمی‌شود.
     * =====================================================
     */

    long countByEmployeeDepartmentIdAndStatusAndEmployeeIdNot(
            Long departmentId,
            LeaveStatus status,
            Long excludedEmployeeId
    );


    /*
     * =====================================================
     * 5 درخواست اخیر واحد مدیر
     *
     * درخواست خود مدیر نمایش داده نمی‌شود.
     * =====================================================
     */

    @EntityGraph(attributePaths = {
            "employee",
            "employee.department"
    })
    List<LeaveRequest>
    findTop5ByEmployeeDepartmentIdAndStatusAndEmployeeIdNotOrderByCreatedAtDescIdDesc(
            Long departmentId,
            LeaveStatus status,
            Long excludedEmployeeId
    );


    /*
     * =====================================================
     * مرخصی مؤثر در یک روز
     * =====================================================
     */

    @EntityGraph(attributePaths = {
            "employee",
            "employee.department"
    })
    @Query("""
            select leaveRequest
            from LeaveRequest leaveRequest
            where leaveRequest.employee.id = :employeeId
              and leaveRequest.status = :status
              and leaveRequest.startDate <= :reportDate
              and leaveRequest.endDate >= :reportDate
            order by leaveRequest.startDate asc,
                     leaveRequest.id asc
            """)
    List<LeaveRequest>
    findEffectiveLeaves(

            @Param("employeeId")
            Long employeeId,

            @Param("reportDate")
            LocalDate reportDate,

            @Param("status")
            LeaveStatus status
    );


    /*
     * =====================================================
     * مرخصی APPROVED در یک روز
     * =====================================================
     */

    default Optional<LeaveRequest>
    findApprovedLeaveOnDate(
            Long employeeId,
            LocalDate reportDate
    ) {


        List<LeaveRequest> leaves =
                findEffectiveLeaves(

                        employeeId,

                        reportDate,

                        LeaveStatus.APPROVED
                );


        if (leaves.isEmpty()) {

            return Optional.empty();
        }


        return Optional.of(
                leaves.get(0)
        );
    }


    /*
     * =====================================================
     * بررسی وجود مرخصی APPROVED در تاریخ
     * =====================================================
     */

    @Query("""
            select count(leaveRequest) > 0
            from LeaveRequest leaveRequest
            where leaveRequest.employee.id = :employeeId
              and leaveRequest.status = 'APPROVED'
              and leaveRequest.startDate <= :reportDate
              and leaveRequest.endDate >= :reportDate
            """)
    boolean existsApprovedLeaveOnDate(

            @Param("employeeId")
            Long employeeId,

            @Param("reportDate")
            LocalDate reportDate
    );


    /*
     * =====================================================
     * مرخصی‌های یک بازه
     * =====================================================
     */

    @EntityGraph(attributePaths = {
            "employee",
            "employee.department"
    })
    @Query("""
            select leaveRequest
            from LeaveRequest leaveRequest
            where leaveRequest.status = :status
              and leaveRequest.startDate <= :toDate
              and leaveRequest.endDate >= :fromDate
            order by leaveRequest.startDate asc,
                     leaveRequest.employee.lastName asc,
                     leaveRequest.employee.firstName asc
            """)
    List<LeaveRequest>
    findLeavesBetween(

            @Param("fromDate")
            LocalDate fromDate,

            @Param("toDate")
            LocalDate toDate,

            @Param("status")
            LeaveStatus status
    );


    /*
     * =====================================================
     * تشخیص همپوشانی
     * =====================================================
     */

    @Query("""
            select leaveRequest
            from LeaveRequest leaveRequest
            where leaveRequest.employee.id = :employeeId
              and leaveRequest.startDate <= :endDate
              and leaveRequest.endDate >= :startDate
              and leaveRequest.status <> 'REJECTED'
            order by leaveRequest.startDate asc
            """)
    List<LeaveRequest>
    findOverlappingActiveRequests(

            @Param("employeeId")
            Long employeeId,

            @Param("startDate")
            LocalDate startDate,

            @Param("endDate")
            LocalDate endDate
    );

}