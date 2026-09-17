package ir.ac.sutech.attendance_system.service;

import ir.ac.sutech.attendance_system.dto.AttendanceMetricRow;
import ir.ac.sutech.attendance_system.dto.PerformanceTaskForm;
import ir.ac.sutech.attendance_system.dto.QuarterlyPerformanceRow;
import ir.ac.sutech.attendance_system.model.Employee;
import ir.ac.sutech.attendance_system.model.PerformanceReviewStatus;
import ir.ac.sutech.attendance_system.model.PerformanceTask;
import ir.ac.sutech.attendance_system.model.QuarterlyPerformanceReview;
import ir.ac.sutech.attendance_system.repository.EmployeeRepository;
import ir.ac.sutech.attendance_system.repository.PerformanceTaskRepository;
import ir.ac.sutech.attendance_system.repository.QuarterlyPerformanceReviewRepository;
import ir.ac.sutech.attendance_system.util.JalaliDateUtil;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class QuarterlyPerformanceService {

    private final QuarterlyPerformanceReviewRepository reviewRepository;
    private final PerformanceTaskRepository taskRepository;
    private final EmployeeRepository employeeRepository;
    private final CurrentEmployeeService currentEmployeeService;
    private final BacklogAttendanceMetricsService attendanceMetricsService;
    private final JalaliDateUtil jalaliDateUtil;

    public QuarterlyPerformanceService(
            QuarterlyPerformanceReviewRepository reviewRepository,
            PerformanceTaskRepository taskRepository,
            EmployeeRepository employeeRepository,
            CurrentEmployeeService currentEmployeeService,
            BacklogAttendanceMetricsService attendanceMetricsService,
            JalaliDateUtil jalaliDateUtil
    ) {
        this.reviewRepository = reviewRepository;
        this.taskRepository = taskRepository;
        this.employeeRepository = employeeRepository;
        this.currentEmployeeService = currentEmployeeService;
        this.attendanceMetricsService = attendanceMetricsService;
        this.jalaliDateUtil = jalaliDateUtil;
    }

    public List<Employee> findManagerEmployees(String username) {
        Employee manager = currentEmployeeService
                .requireEmployee(username);

        Long departmentId = manager.getDepartment().getId();

        return employeeRepository
                .findByActiveTrueOrderByLastNameAscFirstNameAsc()
                .stream()
                .filter(employee -> departmentId.equals(
                        employee.getDepartment().getId()
                ))
                .filter(employee -> !employee.getId().equals(
                        manager.getId()
                ))
                .toList();
    }

    public QuarterlyPerformanceReview findReview(
            Long employeeId,
            int year,
            int quarter
    ) {
        validateYearQuarter(year, quarter);

        return reviewRepository
                .findByEmployeeIdAndYearAndQuarter(
                        employeeId,
                        year,
                        quarter
                )
                .orElse(null);
    }

    public QuarterlyPerformanceRow buildRow(
            QuarterlyPerformanceReview review
    ) {
        if (review == null) {
            return null;
        }

        LocalDate[] range = quarterRange(
                review.getYear(),
                review.getQuarter()
        );

        AttendanceMetricRow attendance =
                attendanceMetricsService.getEmployeeMetrics(
                        review.getEmployee().getId(),
                        range[0],
                        range[1]
                );

        double taskScore = calculateTaskScore(review);
        double attendanceRate = attendance == null
                ? 0.0
                : attendance.getAttendanceRate();

        // تعریف بهره‌وری طبق نیاز پروژه:
        // امتیاز کارهای محول‌شده × نسبت حضور مؤثر در سه‌ماهه.
        double productivity = round(
                taskScore * attendanceRate / 100.0
        );

        return new QuarterlyPerformanceRow(
                review,
                taskScore,
                attendanceRate,
                productivity,
                attendance == null ? 0 : attendance.getPresentDays(),
                attendance == null ? 0 : attendance.getAbsentDays(),
                attendance == null ? 0 : attendance.getIncompleteDays()
        );
    }

    public List<QuarterlyPerformanceRow> findSubmittedRows(
            int year,
            int quarter
    ) {
        validateYearQuarter(year, quarter);

        return reviewRepository
                .findByStatusAndYearAndQuarterOrderByEmployeeLastNameAscEmployeeFirstNameAsc(
                        PerformanceReviewStatus.SUBMITTED,
                        year,
                        quarter
                )
                .stream()
                .map(this::buildRow)
                .toList();
    }

    @Transactional
    public QuarterlyPerformanceReview addTask(
            String managerUsername,
            PerformanceTaskForm form
    ) {
        validateTaskForm(form);

        Employee manager = currentEmployeeService
                .requireEmployee(managerUsername);

        Employee employee = requireEmployeeInManagerDepartment(
                manager,
                form.getEmployeeId()
        );

        QuarterlyPerformanceReview review =
                reviewRepository
                        .findByEmployeeIdAndYearAndQuarter(
                                employee.getId(),
                                form.getYear(),
                                form.getQuarter()
                        )
                        .orElseGet(() ->
                                reviewRepository.save(
                                        new QuarterlyPerformanceReview(
                                                employee,
                                                manager,
                                                form.getYear(),
                                                form.getQuarter()
                                        )
                                )
                        );

        ensureManagerOwnsDraft(manager, review);

        PerformanceTask task = new PerformanceTask(
                review,
                form.getTitle().trim(),
                normalizeNullable(form.getDescription()),
                form.getWeightPercent()
        );

        taskRepository.save(task);

        return review;
    }

    @Transactional
    public void updateCompletion(
            String managerUsername,
            Long taskId,
            int completionPercent
    ) {
        if (completionPercent < 0 || completionPercent > 100) {
            throw new IllegalArgumentException(
                    "درصد انجام کار باید بین صفر تا ۱۰۰ باشد."
            );
        }

        Employee manager = currentEmployeeService
                .requireEmployee(managerUsername);

        PerformanceTask task = taskRepository
                .findById(taskId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "کار ارزیابی پیدا نشد."
                        )
                );

        ensureManagerOwnsDraft(
                manager,
                task.getReview()
        );

        task.setCompletionPercent(completionPercent);
        taskRepository.save(task);
    }

    @Transactional
    public void deleteTask(
            String managerUsername,
            Long taskId
    ) {
        Employee manager = currentEmployeeService
                .requireEmployee(managerUsername);

        PerformanceTask task = taskRepository
                .findById(taskId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "کار ارزیابی پیدا نشد."
                        )
                );

        ensureManagerOwnsDraft(
                manager,
                task.getReview()
        );

        taskRepository.delete(task);
    }

    @Transactional
    public QuarterlyPerformanceReview submitReview(
            String managerUsername,
            Long employeeId,
            int year,
            int quarter,
            String managerNote
    ) {
        Employee manager = currentEmployeeService
                .requireEmployee(managerUsername);

        requireEmployeeInManagerDepartment(
                manager,
                employeeId
        );

        QuarterlyPerformanceReview review =
                reviewRepository
                        .findByEmployeeIdAndYearAndQuarter(
                                employeeId,
                                year,
                                quarter
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "برای این کارمند هنوز ارزیابی سه‌ماهه‌ای ثبت نشده است."
                                )
                        );

        ensureManagerOwnsDraft(manager, review);

        if (review.getTasks() == null
                || review.getTasks().isEmpty()) {
            throw new IllegalArgumentException(
                    "برای ارسال ارزیابی باید حداقل یک کار محول‌شده ثبت شده باشد."
            );
        }

        review.submit(normalizeNullable(managerNote));

        return reviewRepository.save(review);
    }

    public double calculateTaskScore(
            QuarterlyPerformanceReview review
    ) {
        if (review.getTasks() == null
                || review.getTasks().isEmpty()) {
            return 0.0;
        }

        long totalWeight = review.getTasks()
                .stream()
                .mapToLong(PerformanceTask::getWeightPercent)
                .sum();

        if (totalWeight <= 0) {
            return 0.0;
        }

        double weighted = review.getTasks()
                .stream()
                .mapToDouble(task ->
                        task.getWeightPercent()
                                * task.getCompletionPercent()
                )
                .sum();

        return round(weighted / totalWeight);
    }

    public LocalDate[] quarterRange(
            int year,
            int quarter
    ) {
        validateYearQuarter(year, quarter);

        return jalaliDateUtil.jalaliQuarterRange(
                year,
                quarter
        );
    }

    private Employee requireEmployeeInManagerDepartment(
            Employee manager,
            Long employeeId
    ) {
        if (employeeId == null) {
            throw new IllegalArgumentException(
                    "کارمند را انتخاب کنید."
            );
        }

        Employee employee = employeeRepository
                .findById(employeeId)
                .filter(Employee::isActive)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "کارمند انتخاب‌شده پیدا نشد یا غیرفعال است."
                        )
                );

        if (!manager.getDepartment().getId().equals(
                employee.getDepartment().getId()
        )) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "مدیر فقط می‌تواند کارکنان واحد خود را ارزیابی کند."
            );
        }

        if (manager.getId().equals(employee.getId())) {
            throw new IllegalArgumentException(
                    "مدیر نمی‌تواند ارزیابی سه‌ماهه خودش را ثبت کند."
            );
        }

        return employee;
    }

    private void ensureManagerOwnsDraft(
            Employee manager,
            QuarterlyPerformanceReview review
    ) {
        if (!review.getReviewer().getId().equals(
                manager.getId()
        )) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "این ارزیابی توسط مدیر دیگری ایجاد شده است."
            );
        }

        if (!review.isDraft()) {
            throw new IllegalArgumentException(
                    "ارزیابی ارسال‌شده دیگر قابل ویرایش نیست."
            );
        }
    }

    private void validateTaskForm(
            PerformanceTaskForm form
    ) {
        if (form == null) {
            throw new IllegalArgumentException(
                    "اطلاعات کار کامل نیست."
            );
        }

        if (form.getEmployeeId() == null) {
            throw new IllegalArgumentException(
                    "کارمند را انتخاب کنید."
            );
        }

        if (form.getYear() == null
                || form.getQuarter() == null) {
            throw new IllegalArgumentException(
                    "سال و سه‌ماهه را انتخاب کنید."
            );
        }

        validateYearQuarter(
                form.getYear(),
                form.getQuarter()
        );

        if (form.getTitle() == null
                || form.getTitle().isBlank()) {
            throw new IllegalArgumentException(
                    "عنوان کار محول‌شده را وارد کنید."
            );
        }

        if (form.getTitle().trim().length() > 250) {
            throw new IllegalArgumentException(
                    "عنوان کار نمی‌تواند بیشتر از ۲۵۰ کاراکتر باشد."
            );
        }

        if (form.getWeightPercent() == null
                || form.getWeightPercent() < 1
                || form.getWeightPercent() > 100) {
            throw new IllegalArgumentException(
                    "وزن کار باید بین ۱ تا ۱۰۰ باشد."
            );
        }
    }

    private void validateYearQuarter(
            int year,
            int quarter
    ) {
        if (year < 1300 || year > 1600) {
            throw new IllegalArgumentException(
                    "سال شمسی ارزیابی معتبر نیست."
            );
        }

        if (quarter < 1 || quarter > 4) {
            throw new IllegalArgumentException(
                    "سه‌ماهه باید بین ۱ تا ۴ باشد."
            );
        }
    }

    private String normalizeNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
