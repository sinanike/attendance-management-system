package ir.ac.sutech.attendance_system.service;

import ir.ac.sutech.attendance_system.dto.EmployeeForm;
import ir.ac.sutech.attendance_system.model.Department;
import ir.ac.sutech.attendance_system.model.Employee;
import ir.ac.sutech.attendance_system.model.EmployeeType;
import ir.ac.sutech.attendance_system.model.Shift;
import ir.ac.sutech.attendance_system.repository.DepartmentRepository;
import ir.ac.sutech.attendance_system.repository.EmployeeRepository;
import ir.ac.sutech.attendance_system.repository.ShiftRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final ShiftRepository shiftRepository;

    public EmployeeService(
            EmployeeRepository employeeRepository,
            DepartmentRepository departmentRepository,
            ShiftRepository shiftRepository
    ) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.shiftRepository = shiftRepository;
    }

    public List<Employee> findAllEmployees() {
        return employeeRepository.findAllByOrderByIdAsc();
    }

    public long countEmployees() {
        return employeeRepository.count();
    }

    public List<Department> findActiveDepartments() {
        return departmentRepository
                .findAllByActiveTrueOrderByNameAsc();
    }

    public List<Shift> findActiveShifts() {
        return shiftRepository
                .findAllByActiveTrueOrderByNameAsc();
    }

    public boolean personnelCodeExists(String personnelCode) {
        if (personnelCode == null || personnelCode.isBlank()) {
            return false;
        }

        return employeeRepository.existsByPersonnelCodeIgnoreCase(
                personnelCode.trim()
        );
    }

    public boolean nationalCodeExists(String nationalCode) {
        if (nationalCode == null || nationalCode.isBlank()) {
            return false;
        }

        return employeeRepository.existsByNationalCode(
                nationalCode.trim()
        );
    }

    public boolean deviceUserIdExists(String deviceUserId) {
        if (deviceUserId == null || deviceUserId.isBlank()) {
            return false;
        }

        return employeeRepository.existsByDeviceUserId(
                deviceUserId.trim()
        );
    }

    public boolean personnelCodeExistsForAnotherEmployee(
            String personnelCode,
            Long employeeId
    ) {
        if (personnelCode == null || personnelCode.isBlank()) {
            return false;
        }

        return employeeRepository
                .existsByPersonnelCodeIgnoreCaseAndIdNot(
                        personnelCode.trim(),
                        employeeId
                );
    }

    public boolean nationalCodeExistsForAnotherEmployee(
            String nationalCode,
            Long employeeId
    ) {
        if (nationalCode == null || nationalCode.isBlank()) {
            return false;
        }

        return employeeRepository
                .existsByNationalCodeAndIdNot(
                        nationalCode.trim(),
                        employeeId
                );
    }

    public boolean deviceUserIdExistsForAnotherEmployee(
            String deviceUserId,
            Long employeeId
    ) {
        if (deviceUserId == null || deviceUserId.isBlank()) {
            return false;
        }

        return employeeRepository
                .existsByDeviceUserIdAndIdNot(
                        deviceUserId.trim(),
                        employeeId
                );
    }

    public EmployeeForm findEmployeeFormById(Long employeeId) {
        Employee employee = findEmployeeOrThrow(employeeId);

        EmployeeForm form = new EmployeeForm();

        form.setPersonnelCode(employee.getPersonnelCode());
        form.setDeviceUserId(employee.getDeviceUserId());
        form.setNationalCode(employee.getNationalCode());
        form.setFirstName(employee.getFirstName());
        form.setLastName(employee.getLastName());
        form.setEmployeeType(employee.getEmployeeType());
        form.setDepartmentId(employee.getDepartment().getId());

        if (employee.getShift() != null) {
            form.setShiftId(employee.getShift().getId());
        }

        return form;
    }

    @Transactional
    public Employee createEmployee(EmployeeForm form) {
        Department department = findActiveDepartment(
                form.getDepartmentId()
        );

        Shift shift = resolveShift(
                form.getEmployeeType(),
                form.getShiftId()
        );

        Employee employee = new Employee(
                normalizePersonnelCode(form.getPersonnelCode()),
                form.getNationalCode().trim(),
                form.getFirstName().trim(),
                form.getLastName().trim(),
                department
        );

        employee.setDeviceUserId(
                normalizeDeviceUserId(form.getDeviceUserId())
        );

        employee.setEmployeeType(form.getEmployeeType());
        employee.setShift(shift);

        return employeeRepository.save(employee);
    }

    @Transactional
    public Employee updateEmployee(
            Long employeeId,
            EmployeeForm form
    ) {
        Employee employee = findEmployeeOrThrow(employeeId);

        Department department = findActiveDepartment(
                form.getDepartmentId()
        );

        Shift shift = resolveShift(
                form.getEmployeeType(),
                form.getShiftId()
        );

        employee.setPersonnelCode(
                normalizePersonnelCode(form.getPersonnelCode())
        );

        employee.setDeviceUserId(
                normalizeDeviceUserId(form.getDeviceUserId())
        );

        employee.setNationalCode(
                form.getNationalCode().trim()
        );

        employee.setFirstName(
                form.getFirstName().trim()
        );

        employee.setLastName(
                form.getLastName().trim()
        );

        employee.setEmployeeType(form.getEmployeeType());
        employee.setDepartment(department);
        employee.setShift(shift);

        return employeeRepository.save(employee);
    }

    @Transactional
    public Employee updateEmployeeStatus(
            Long employeeId,
            boolean active
    ) {
        Employee employee = findEmployeeOrThrow(employeeId);

        employee.setActive(active);

        return employeeRepository.save(employee);
    }

    private Employee findEmployeeOrThrow(Long employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "کارمند موردنظر پیدا نشد"
                ));
    }

    private Department findActiveDepartment(Long departmentId) {
        if (departmentId == null) {
            throw new IllegalArgumentException(
                    "واحد سازمانی را انتخاب کنید"
            );
        }

        return departmentRepository.findById(departmentId)
                .filter(Department::isActive)
                .orElseThrow(() -> new IllegalArgumentException(
                        "واحد سازمانی انتخاب‌شده معتبر نیست"
                ));
    }

    private Shift findActiveShift(Long shiftId) {
        if (shiftId == null) {
            throw new IllegalArgumentException(
                    "برای خدمات و پشتیبانی، انتخاب شیفت کاری الزامی است"
            );
        }

        return shiftRepository.findById(shiftId)
                .filter(Shift::isActive)
                .orElseThrow(() -> new IllegalArgumentException(
                        "شیفت کاری انتخاب‌شده معتبر نیست"
                ));
    }

    private Shift resolveShift(
            EmployeeType employeeType,
            Long shiftId
    ) {
        if (employeeType == null) {
            throw new IllegalArgumentException(
                    "نوع نیرو را انتخاب کنید"
            );
        }

        if (employeeType == EmployeeType.ADMINISTRATIVE) {
            return null;
        }

        if (employeeType == EmployeeType.SERVICE_SUPPORT) {
            return findActiveShift(shiftId);
        }

        throw new IllegalArgumentException(
                "نوع نیروی انتخاب‌شده معتبر نیست"
        );
    }

    private String normalizePersonnelCode(String personnelCode) {
        return personnelCode
                .trim()
                .toUpperCase(Locale.ENGLISH);
    }

    private String normalizeDeviceUserId(String deviceUserId) {
        if (deviceUserId == null || deviceUserId.isBlank()) {
            return null;
        }

        return deviceUserId.trim();
    }
}