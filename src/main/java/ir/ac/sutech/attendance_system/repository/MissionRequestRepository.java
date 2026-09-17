package ir.ac.sutech.attendance_system.repository;

import ir.ac.sutech.attendance_system.model.MissionRequest;
import ir.ac.sutech.attendance_system.model.MissionStatus;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

import java.util.List;
import java.util.Optional;


public interface MissionRequestRepository
        extends JpaRepository<MissionRequest, Long> {


    /*
     * =====================================================
     * تمام مأموریت‌ها
     * ADMIN
     * =====================================================
     */

    @EntityGraph(attributePaths = {
            "employee",
            "employee.department"
    })
    List<MissionRequest>
    findAllByOrderByCreatedAtDescIdDesc();


    /*
     * =====================================================
     * مأموریت‌های یک Employee
     * =====================================================
     */

    @EntityGraph(attributePaths = {
            "employee",
            "employee.department"
    })
    List<MissionRequest>
    findByEmployeeIdOrderByCreatedAtDescIdDesc(
            Long employeeId
    );


    /*
     * =====================================================
     * دریافت امن یک Mission برای Employee
     * =====================================================
     */

    @EntityGraph(attributePaths = {
            "employee",
            "employee.department"
    })
    Optional<MissionRequest>
    findByIdAndEmployeeId(
            Long id,
            Long employeeId
    );


    /*
     * =====================================================
     * درخواست‌های براساس Status
     * ADMIN
     * =====================================================
     */

    @EntityGraph(attributePaths = {
            "employee",
            "employee.department"
    })
    List<MissionRequest>
    findByStatusOrderByCreatedAtDescIdDesc(
            MissionStatus status
    );


    /*
     * =====================================================
     * مأموریت‌های Department
     * =====================================================
     */

    @EntityGraph(attributePaths = {
            "employee",
            "employee.department"
    })
    List<MissionRequest>
    findByEmployeeDepartmentIdOrderByCreatedAtDescIdDesc(
            Long departmentId
    );


    /*
     * =====================================================
     * مأموریت‌های Department براساس Status
     * =====================================================
     */

    @EntityGraph(attributePaths = {
            "employee",
            "employee.department"
    })
    List<MissionRequest>
    findByEmployeeDepartmentIdAndStatusOrderByCreatedAtDescIdDesc(
            Long departmentId,
            MissionStatus status
    );


    /*
     * =====================================================
     * دریافت امن درخواست توسط Manager
     * =====================================================
     */

    @EntityGraph(attributePaths = {
            "employee",
            "employee.department"
    })
    Optional<MissionRequest>
    findByIdAndEmployeeDepartmentId(
            Long id,
            Long departmentId
    );


    /*
     * =====================================================
     * تعداد مأموریت‌های Pending واحد
     *
     * درخواست خود مدیر حذف می‌شود.
     * =====================================================
     */

    long countByEmployeeDepartmentIdAndStatusAndEmployeeIdNot(
            Long departmentId,
            MissionStatus status,
            Long excludedEmployeeId
    );


    /*
     * =====================================================
     * پنج مأموریت Pending اخیر
     * =====================================================
     */

    @EntityGraph(attributePaths = {
            "employee",
            "employee.department"
    })
    List<MissionRequest>
    findTop5ByEmployeeDepartmentIdAndStatusAndEmployeeIdNotOrderByCreatedAtDescIdDesc(
            Long departmentId,
            MissionStatus status,
            Long excludedEmployeeId
    );


    /*
     * =====================================================
     * مأموریت مؤثر در یک روز
     * =====================================================
     */

    @EntityGraph(attributePaths = {
            "employee",
            "employee.department"
    })
    @Query("""
            select mission
            from MissionRequest mission
            where mission.employee.id = :employeeId
              and mission.status = :status
              and mission.startDate <= :reportDate
              and mission.endDate >= :reportDate
            order by mission.id desc
            """)
    List<MissionRequest>
    findEffectiveMissions(

            @Param("employeeId")
            Long employeeId,

            @Param("reportDate")
            LocalDate reportDate,

            @Param("status")
            MissionStatus status
    );


    /*
     * =====================================================
     * مأموریت APPROVED در تاریخ
     * =====================================================
     */

    default Optional<MissionRequest>
    findApprovedMissionOnDate(
            Long employeeId,
            LocalDate reportDate
    ) {


        List<MissionRequest> missions =
                findEffectiveMissions(

                        employeeId,

                        reportDate,

                        MissionStatus.APPROVED
                );


        if (missions.isEmpty()) {

            return Optional.empty();
        }


        return Optional.of(
                missions.get(0)
        );
    }


    /*
     * =====================================================
     * مأموریت‌های همپوشان
     * =====================================================
     */

    @Query("""
            select mission
            from MissionRequest mission
            where mission.employee.id = :employeeId
              and mission.status <> :rejectedStatus
              and mission.startDate <= :endDate
              and mission.endDate >= :startDate
            """)
    List<MissionRequest>
    findOverlappingActiveRequests(

            @Param("employeeId")
            Long employeeId,

            @Param("startDate")
            LocalDate startDate,

            @Param("endDate")
            LocalDate endDate,

            @Param("rejectedStatus")
            MissionStatus rejectedStatus
    );

}