package ir.ac.sutech.attendance_system.repository;

import ir.ac.sutech.attendance_system.model.Employee;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository
        extends JpaRepository<Employee, Long> {


    /*
     * ==========================================
     * همه کارکنان
     * ==========================================
     */

    @EntityGraph(attributePaths = {
            "department",
            "shift"
    })
    List<Employee> findAllByOrderByIdAsc();


    /*
     * ==========================================
     * همه کارکنان فعال
     * ==========================================
     */

    @EntityGraph(attributePaths = {
            "department",
            "shift"
    })
    List<Employee>
    findByActiveTrueOrderByLastNameAscFirstNameAsc();


    /*
     * ==========================================
     * همه کارکنان یک واحد سازمانی
     *
     * برای پنل مدیر بخش
     * ==========================================
     */

    @EntityGraph(attributePaths = {
            "department",
            "shift"
    })
    List<Employee>
    findByDepartmentIdOrderByLastNameAscFirstNameAsc(
            Long departmentId
    );


    /*
     * ==========================================
     * کارکنان فعال یک واحد سازمانی
     *
     * برای گزارش حضور و غیاب مدیر بخش
     * ==========================================
     */

    @EntityGraph(attributePaths = {
            "department",
            "shift"
    })
    List<Employee>
    findByDepartmentIdAndActiveTrueOrderByLastNameAscFirstNameAsc(
            Long departmentId
    );


    /*
     * ==========================================
     * دستگاه اثر انگشت
     * ==========================================
     */

    Optional<Employee> findByDeviceUserId(
            String deviceUserId
    );


    /*
     * ==========================================
     * بررسی تکراری بودن اطلاعات
     * ==========================================
     */

    boolean existsByPersonnelCodeIgnoreCase(
            String personnelCode
    );


    boolean existsByNationalCode(
            String nationalCode
    );


    boolean existsByDeviceUserId(
            String deviceUserId
    );


    boolean existsByPersonnelCodeIgnoreCaseAndIdNot(
            String personnelCode,
            Long id
    );


    boolean existsByNationalCodeAndIdNot(
            String nationalCode,
            Long id
    );


    boolean existsByDeviceUserIdAndIdNot(
            String deviceUserId,
            Long id
    );
}