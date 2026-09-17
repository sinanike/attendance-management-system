package ir.ac.sutech.attendance_system.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(
        name = "employee_shift_assignments",
        indexes = {
                @Index(
                        name = "idx_shift_assignments_employee",
                        columnList = "employee_id"
                ),
                @Index(
                        name = "idx_shift_assignments_shift",
                        columnList = "shift_id"
                ),
                @Index(
                        name = "idx_shift_assignments_dates",
                        columnList = "start_date,end_date"
                ),
                @Index(
                        name = "idx_shift_assignments_employee_dates",
                        columnList = "employee_id,start_date,end_date"
                ),
                @Index(
                        name = "idx_shift_assignments_active",
                        columnList = "active"
                )
        }
)
public class EmployeeShiftAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "employee_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_shift_assignments_employee"
            )
    )
    private Employee employee;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "shift_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_shift_assignments_shift"
            )
    )
    private Shift shift;

    /*
     * اولین روز اجرای شیفت
     */
    @Column(
            name = "start_date",
            nullable = false
    )
    private LocalDate startDate;

    /*
     * آخرین روز اجرای شیفت
     *
     * اگر null باشد یعنی پایان مشخصی ندارد.
     */
    @Column(name = "end_date")
    private LocalDate endDate;

    /*
     * active به معنی معتبر بودن سابقه است،
     * نه اینکه هنوز تاریخ آن تمام نشده باشد.
     *
     * بنابراین پایان یافتن بازه باعث false شدن
     * این فیلد نمی‌شود.
     *
     * false فقط برای لغو یک تخصیص اشتباه است.
     */
    @Column(nullable = false)
    private boolean active = true;

    protected EmployeeShiftAssignment() {
    }

    public EmployeeShiftAssignment(
            Employee employee,
            Shift shift,
            LocalDate startDate,
            LocalDate endDate
    ) {
        setEmployee(employee);
        setShift(shift);
        setStartDate(startDate);
        setEndDate(endDate);
    }

    public static EmployeeShiftAssignment forSingleDay(
            Employee employee,
            Shift shift,
            LocalDate workDate
    ) {
        return new EmployeeShiftAssignment(
                employee,
                shift,
                workDate,
                workDate
        );
    }

    public Long getId() {
        return id;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(
            Employee employee
    ) {

        if (employee == null) {
            throw new IllegalArgumentException(
                    "کارمند برای تخصیص شیفت الزامی است."
            );
        }

        this.employee = employee;
    }

    public Shift getShift() {
        return shift;
    }

    public void setShift(
            Shift shift
    ) {

        if (shift == null) {
            throw new IllegalArgumentException(
                    "شیفت برای تخصیص الزامی است."
            );
        }

        this.shift = shift;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(
            LocalDate startDate
    ) {

        if (startDate == null) {
            throw new IllegalArgumentException(
                    "تاریخ شروع تخصیص شیفت الزامی است."
            );
        }

        if (
                endDate != null
                        && endDate.isBefore(startDate)
        ) {
            throw new IllegalArgumentException(
                    "تاریخ شروع نمی‌تواند بعد از تاریخ پایان باشد."
            );
        }

        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(
            LocalDate endDate
    ) {

        if (
                endDate != null
                        && startDate != null
                        && endDate.isBefore(startDate)
        ) {
            throw new IllegalArgumentException(
                    "تاریخ پایان نمی‌تواند قبل از تاریخ شروع باشد."
            );
        }

        this.endDate = endDate;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(
            boolean active
    ) {
        this.active = active;
    }

    /*
     * بررسی می‌کند این شیفت در تاریخ موردنظر
     * برای کارمند معتبر بوده یا نه.
     */
    public boolean isEffectiveOn(
            LocalDate date
    ) {

        if (
                date == null
                        || !active
                        || startDate == null
        ) {
            return false;
        }

        boolean started =
                !date.isBefore(startDate);

        boolean notEnded =
                endDate == null
                        || !date.isAfter(endDate);

        return started && notEnded;
    }

    public boolean isSingleDayAssignment() {

        return startDate != null
                && endDate != null
                && startDate.equals(endDate);
    }
}