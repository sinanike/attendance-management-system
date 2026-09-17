package ir.ac.sutech.attendance_system.controller;

import ir.ac.sutech.attendance_system.dto.AttendanceMetricRow;
import ir.ac.sutech.attendance_system.dto.PerformanceTaskForm;
import ir.ac.sutech.attendance_system.dto.QuarterlyPerformanceRow;
import ir.ac.sutech.attendance_system.model.AttendanceRuleSettings;
import ir.ac.sutech.attendance_system.model.Employee;
import ir.ac.sutech.attendance_system.model.QuarterlyPerformanceReview;
import ir.ac.sutech.attendance_system.service.AttendanceRuleSettingsService;
import ir.ac.sutech.attendance_system.service.BacklogAttendanceMetricsService;
import ir.ac.sutech.attendance_system.service.CurrentEmployeeService;
import ir.ac.sutech.attendance_system.service.QuarterlyPerformanceService;
import ir.ac.sutech.attendance_system.util.JalaliDateUtil;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/manager")
public class ManagerBacklogController {

    private final CurrentEmployeeService currentEmployeeService;
    private final BacklogAttendanceMetricsService attendanceMetricsService;
    private final AttendanceRuleSettingsService ruleSettingsService;
    private final QuarterlyPerformanceService performanceService;
    private final JalaliDateUtil jalaliDateUtil;

    public ManagerBacklogController(
            CurrentEmployeeService currentEmployeeService,
            BacklogAttendanceMetricsService attendanceMetricsService,
            AttendanceRuleSettingsService ruleSettingsService,
            QuarterlyPerformanceService performanceService,
            JalaliDateUtil jalaliDateUtil
    ) {
        this.currentEmployeeService = currentEmployeeService;
        this.attendanceMetricsService = attendanceMetricsService;
        this.ruleSettingsService = ruleSettingsService;
        this.performanceService = performanceService;
        this.jalaliDateUtil = jalaliDateUtil;
    }

    @GetMapping("/overtime")
    public String overtime(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate toDate,
            Principal principal,
            Model model
    ) {
        LocalDate[] range = defaultMonthRange(fromDate, toDate);
        Employee manager = currentEmployeeService
                .requireEmployee(principal.getName());

        List<AttendanceMetricRow> rows =
                attendanceMetricsService.getOvertimeRows(
                        manager.getDepartment().getId(),
                        range[0],
                        range[1]
                );

        long totalOvertimeMinutes = rows.stream()
                .mapToLong(AttendanceMetricRow::getOvertimeMinutes)
                .sum();

        model.addAttribute("manager", manager);
        model.addAttribute("department", manager.getDepartment());
        model.addAttribute("fromDate", range[0]);
        model.addAttribute("toDate", range[1]);
        model.addAttribute("rows", rows);
        model.addAttribute("totalOvertimeHours", totalOvertimeMinutes / 60);
        model.addAttribute("totalOvertimeRemainingMinutes", totalOvertimeMinutes % 60);

        return "manager-overtime";
    }

    @GetMapping("/insufficient-attendance")
    public String insufficientAttendance(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate toDate,
            Principal principal,
            Model model
    ) {
        LocalDate[] range = defaultMonthRange(fromDate, toDate);
        Employee manager = currentEmployeeService
                .requireEmployee(principal.getName());

        AttendanceRuleSettings settings =
                ruleSettingsService.getSettings();

        List<AttendanceMetricRow> rows =
                attendanceMetricsService.getInsufficientRows(
                        manager.getDepartment().getId(),
                        range[0],
                        range[1],
                        settings.getMinimumAttendancePercent()
                );

        model.addAttribute("manager", manager);
        model.addAttribute("department", manager.getDepartment());
        model.addAttribute("fromDate", range[0]);
        model.addAttribute("toDate", range[1]);
        model.addAttribute("minimumAttendancePercent",
                settings.getMinimumAttendancePercent());
        model.addAttribute("rows", rows);

        return "manager-insufficient-attendance";
    }

    @GetMapping("/performance")
    public String performance(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer quarter,
            Principal principal,
            Model model
    ) {
        int selectedYear = year == null
                ? jalaliDateUtil.currentJalaliYear()
                : year;
        int selectedQuarter = quarter == null
                ? jalaliDateUtil.currentJalaliQuarter()
                : quarter;

        Employee manager = currentEmployeeService
                .requireEmployee(principal.getName());

        List<Employee> employees = performanceService
                .findManagerEmployees(principal.getName());

        QuarterlyPerformanceReview review = null;
        QuarterlyPerformanceRow performanceRow = null;

        if (employeeId != null) {
            boolean allowed = employees.stream()
                    .anyMatch(employee -> employeeId.equals(employee.getId()));

            if (!allowed) {
                throw new IllegalArgumentException(
                        "کارمند انتخاب‌شده در واحد شما قرار ندارد."
                );
            }

            review = performanceService.findReview(
                    employeeId,
                    selectedYear,
                    selectedQuarter
            );

            performanceRow = performanceService.buildRow(review);
        }

        LocalDate[] quarterRange = performanceService.quarterRange(
                selectedYear,
                selectedQuarter
        );

        model.addAttribute("manager", manager);
        model.addAttribute("department", manager.getDepartment());
        model.addAttribute("employees", employees);
        model.addAttribute("selectedEmployeeId", employeeId);
        model.addAttribute("year", selectedYear);
        model.addAttribute("quarter", selectedQuarter);
        model.addAttribute("quarterFromDate", quarterRange[0]);
        model.addAttribute("quarterToDate", quarterRange[1]);
        model.addAttribute("review", review);
        model.addAttribute("performanceRow", performanceRow);
        model.addAttribute("taskForm", new PerformanceTaskForm());

        return "manager-performance";
    }

    @PostMapping("/performance/tasks")
    public String addTask(
            PerformanceTaskForm form,
            Principal principal,
            RedirectAttributes redirectAttributes
    ) {
        try {
            performanceService.addTask(
                    principal.getName(),
                    form
            );
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "کار محول‌شده ثبت شد."
            );
        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    exception.getMessage()
            );
        }

        return performanceRedirect(
                form.getEmployeeId(),
                form.getYear(),
                form.getQuarter()
        );
    }

    @PostMapping("/performance/tasks/{taskId}/completion")
    public String updateTaskCompletion(
            @PathVariable Long taskId,
            @RequestParam int completionPercent,
            @RequestParam Long employeeId,
            @RequestParam int year,
            @RequestParam int quarter,
            Principal principal,
            RedirectAttributes redirectAttributes
    ) {
        try {
            performanceService.updateCompletion(
                    principal.getName(),
                    taskId,
                    completionPercent
            );
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "درصد انجام کار به‌روزرسانی شد."
            );
        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    exception.getMessage()
            );
        }

        return performanceRedirect(employeeId, year, quarter);
    }

    @PostMapping("/performance/tasks/{taskId}/delete")
    public String deleteTask(
            @PathVariable Long taskId,
            @RequestParam Long employeeId,
            @RequestParam int year,
            @RequestParam int quarter,
            Principal principal,
            RedirectAttributes redirectAttributes
    ) {
        try {
            performanceService.deleteTask(
                    principal.getName(),
                    taskId
            );
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "کار از ارزیابی حذف شد."
            );
        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    exception.getMessage()
            );
        }

        return performanceRedirect(employeeId, year, quarter);
    }

    @PostMapping("/performance/submit")
    public String submitPerformance(
            @RequestParam Long employeeId,
            @RequestParam int year,
            @RequestParam int quarter,
            @RequestParam(required = false) String managerNote,
            Principal principal,
            RedirectAttributes redirectAttributes
    ) {
        try {
            performanceService.submitReview(
                    principal.getName(),
                    employeeId,
                    year,
                    quarter,
                    managerNote
            );
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "گزارش عملکرد سه‌ماهه برای منابع انسانی ارسال شد."
            );
        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    exception.getMessage()
            );
        }

        return performanceRedirect(employeeId, year, quarter);
    }

    private String performanceRedirect(
            Long employeeId,
            Integer year,
            Integer quarter
    ) {
        StringBuilder target = new StringBuilder(
                "redirect:/manager/performance"
        );

        if (employeeId != null && year != null && quarter != null) {
            target.append("?employeeId=")
                    .append(employeeId)
                    .append("&year=")
                    .append(year)
                    .append("&quarter=")
                    .append(quarter);
        }

        return target.toString();
    }

    private LocalDate[] defaultMonthRange(
            LocalDate fromDate,
            LocalDate toDate
    ) {
        LocalDate[] currentJalaliMonth =
                jalaliDateUtil.currentJalaliMonthRange();

        if (fromDate == null) {
            fromDate = currentJalaliMonth[0];
        }
        if (toDate == null) {
            toDate = currentJalaliMonth[1];
        }

        return new LocalDate[]{fromDate, toDate};
    }
}
