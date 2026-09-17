package ir.ac.sutech.attendance_system.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(
        name = "employees",
        indexes = {
                @Index(
                        name = "idx_employees_department",
                        columnList = "department_id"
                ),
                @Index(
                        name = "idx_employees_shift",
                        columnList = "shift_id"
                ),
                @Index(
                        name = "idx_employees_active",
                        columnList = "active"
                )
        }
)
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "personnel_code",
            nullable = false,
            unique = true,
            length = 30
    )
    private String personnelCode;

    /*
     * شناسه کارمند داخل دستگاه اثر انگشت
     */
    @Column(
            name = "device_user_id",
            unique = true,
            length = 50
    )
    private String deviceUserId;

    @Column(
            name = "national_code",
            nullable = false,
            unique = true,
            length = 10
    )
    private String nationalCode;

    @Column(
            name = "first_name",
            nullable = false,
            length = 50
    )
    private String firstName;

    @Column(
            name = "last_name",
            nullable = false,
            length = 70
    )
    private String lastName;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "department_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_employees_department"
            )
    )
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "shift_id",
            foreignKey = @ForeignKey(
                    name = "fk_employees_shift"
            )
    )
    private Shift shift;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "employee_type",
            length = 30
    )
    private EmployeeType employeeType;

    @Column(nullable = false)
    private boolean active = true;

    protected Employee() {
    }

    public Employee(
            String personnelCode,
            String nationalCode,
            String firstName,
            String lastName,
            Department department
    ) {
        this.personnelCode = personnelCode;
        this.nationalCode = nationalCode;
        this.firstName = firstName;
        this.lastName = lastName;
        this.department = department;
    }

    public Long getId() {
        return id;
    }

    public String getPersonnelCode() {
        return personnelCode;
    }

    public void setPersonnelCode(String personnelCode) {
        this.personnelCode = personnelCode;
    }

    public String getDeviceUserId() {
        return deviceUserId;
    }

    public void setDeviceUserId(String deviceUserId) {

        if (deviceUserId == null || deviceUserId.isBlank()) {
            this.deviceUserId = null;
            return;
        }

        this.deviceUserId = deviceUserId.trim();
    }

    public String getNationalCode() {
        return nationalCode;
    }

    public void setNationalCode(String nationalCode) {
        this.nationalCode = nationalCode;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public Shift getShift() {
        return shift;
    }

    public void setShift(Shift shift) {
        this.shift = shift;
    }

    public EmployeeType getEmployeeType() {
        return employeeType;
    }

    public void setEmployeeType(EmployeeType employeeType) {
        this.employeeType = employeeType;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}