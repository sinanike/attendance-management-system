package ir.ac.sutech.attendance_system.controller;

import ir.ac.sutech.attendance_system.dto.AttendanceMetricRow;
import ir.ac.sutech.attendance_system.dto.AttendanceRuleSettingsForm;
import ir.ac.sutech.attendance_system.dto.QuarterlyPerformanceRow;
import ir.ac.sutech.attendance_system.model.AttendanceRuleSettings;
import ir.ac.sutech.attendance_system.model.Employee;
import ir.ac.sutech.attendance_system.model.EmployeeType;
import ir.ac.sutech.attendance_system.repository.EmployeeRepository;
import ir.ac.sutech.attendance_system.service.AttendanceRuleSettingsService;
import ir.ac.sutech.attendance_system.service.BacklogAttendanceMetricsService;
import ir.ac.sutech.attendance_system.service.QuarterlyPerformanceService;
import ir.ac.sutech.attendance_system.util.JalaliDateUtil;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminBacklogController {

    private final AttendanceRuleSettingsService ruleSettingsService;
    private final BacklogAttendanceMetricsService attendanceMetricsService;
    private final QuarterlyPerformanceService performanceService;
    private final JalaliDateUtil jalaliDateUtil;
    private final EmployeeRepository employeeRepository;

    public AdminBacklogController(
            AttendanceRuleSettingsService ruleSettingsService,
            BacklogAttendanceMetricsService attendanceMetricsService,
            QuarterlyPerformanceService performanceService,
            EmployeeRepository employeeRepository,
            JalaliDateUtil jalaliDateUtil
    ) {
        this.ruleSettingsService = ruleSettingsService;
        this.attendanceMetricsService = attendanceMetricsService;
        this.performanceService = performanceService;
        this.employeeRepository = employeeRepository;
        this.jalaliDateUtil = jalaliDateUtil;
    }

    @GetMapping("/attendance-rules")
    public String attendanceRules(Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute(
                    "form",
                    ruleSettingsService.getForm()
            );
        }

        model.addAttribute(
                "settings",
                ruleSettingsService.getSettings()
        );

        return "admin-attendance-rules";
    }

    @PostMapping("/attendance-rules")
    public String saveAttendanceRules(
            @ModelAttribute("form")
            AttendanceRuleSettingsForm form,
            RedirectAttributes redirectAttributes
    ) {
        try {
            ruleSettingsService.save(form);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "قوانین حضور و غیاب به‌روزرسانی شد."
            );
        } catch (RuntimeException exception) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    exception.getMessage()
            );
            redirectAttributes.addFlashAttribute(
                    "form",
                    form
            );
        }

        return "redirect:/admin/attendance-rules";
    }

    @GetMapping("/overtime")
    public String overtime(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate toDate,
            Model model
    ) {
        LocalDate[] range = defaultMonthRange(fromDate, toDate);

        List<AttendanceMetricRow> rows =
                attendanceMetricsService.getOvertimeRows(
                        null,
                        range[0],
                        range[1]
                );

        long totalOvertimeMinutes = rows.stream()
                .mapToLong(AttendanceMetricRow::getOvertimeMinutes)
                .sum();

        model.addAttribute("fromDate", range[0]);
        model.addAttribute("toDate", range[1]);
        model.addAttribute("rows", rows);
        model.addAttribute("totalOvertimeHours", totalOvertimeMinutes / 60);
        model.addAttribute("totalOvertimeRemainingMinutes", totalOvertimeMinutes % 60);

        return "admin-overtime";
    }

    @GetMapping("/insufficient-attendance")
    public String insufficientAttendance(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate toDate,
            Model model
    ) {
        LocalDate[] range = defaultMonthRange(fromDate, toDate);
        AttendanceRuleSettings settings =
                ruleSettingsService.getSettings();

        List<AttendanceMetricRow> rows =
                attendanceMetricsService.getInsufficientRows(
                        null,
                        range[0],
                        range[1],
                        settings.getMinimumAttendancePercent()
                );

        model.addAttribute("fromDate", range[0]);
        model.addAttribute("toDate", range[1]);
        model.addAttribute("minimumAttendancePercent",
                settings.getMinimumAttendancePercent());
        model.addAttribute("rows", rows);

        return "admin-insufficient-attendance";
    }

    @GetMapping({"/performance-reports", "/productivity"})
    public String performanceReports(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer quarter,
            Model model
    ) {
        int selectedYear = year == null
                ? jalaliDateUtil.currentJalaliYear()
                : year;
        int selectedQuarter = quarter == null
                ? jalaliDateUtil.currentJalaliQuarter()
                : quarter;

        List<QuarterlyPerformanceRow> rows =
                performanceService.findSubmittedRows(
                        selectedYear,
                        selectedQuarter
                );

        double averageProductivity = rows.stream()
                .mapToDouble(
                        QuarterlyPerformanceRow::getProductivityScore
                )
                .average()
                .orElse(0.0);

        double averageTaskScore = rows.stream()
                .mapToDouble(
                        QuarterlyPerformanceRow::getTaskScore
                )
                .average()
                .orElse(0.0);

        double averageAttendanceRate = rows.stream()
                .mapToDouble(
                        QuarterlyPerformanceRow::getAttendanceRate
                )
                .average()
                .orElse(0.0);

        LocalDate[] quarterRange = performanceService.quarterRange(
                selectedYear,
                selectedQuarter
        );

        model.addAttribute("year", selectedYear);
        model.addAttribute("quarter", selectedQuarter);
        model.addAttribute("quarterFromDate", quarterRange[0]);
        model.addAttribute("quarterToDate", quarterRange[1]);
        model.addAttribute("rows", rows);
        model.addAttribute("averageProductivity", round(averageProductivity));
        model.addAttribute("averageTaskScore", round(averageTaskScore));
        model.addAttribute("averageAttendanceRate", round(averageAttendanceRate));

        return "admin-performance-reports";
    }

    @GetMapping("/hr-statistics")
    public String hrStatistics(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate toDate,
            Model model
    ) {
        LocalDate[] range = defaultMonthRange(fromDate, toDate);

        List<Employee> employees = employeeRepository
                .findByActiveTrueOrderByLastNameAscFirstNameAsc();

        List<AttendanceMetricRow> attendanceRows =
                attendanceMetricsService.buildRows(
                        range[0],
                        range[1],
                        null,
                        false
                );

        List<AttendanceMetricRow> overtimeRows =
                attendanceMetricsService.getOvertimeRows(
                        null,
                        range[0],
                        range[1]
                );

        AttendanceRuleSettings settings =
                ruleSettingsService.getSettings();

        List<AttendanceMetricRow> insufficientRows =
                attendanceMetricsService.getInsufficientRows(
                        null,
                        range[0],
                        range[1],
                        settings.getMinimumAttendancePercent()
                );

        long administrativeCount = employees.stream()
                .filter(employee -> employee.getEmployeeType()
                        == EmployeeType.ADMINISTRATIVE)
                .count();

        long serviceCount = employees.stream()
                .filter(employee -> employee.getEmployeeType()
                        == EmployeeType.SERVICE_SUPPORT)
                .count();

        long totalOvertimeMinutes = overtimeRows.stream()
                .mapToLong(AttendanceMetricRow::getOvertimeMinutes)
                .sum();

        double averageAttendanceRate = attendanceRows.stream()
                .filter(row -> row.getAttendanceDenominatorDays() > 0)
                .mapToDouble(AttendanceMetricRow::getAttendanceRate)
                .average()
                .orElse(0.0);

        long totalLateMinutes = attendanceRows.stream()
                .mapToLong(AttendanceMetricRow::getLateMinutes)
                .sum();

        long totalEarlyLeaveMinutes = attendanceRows.stream()
                .mapToLong(AttendanceMetricRow::getEarlyLeaveMinutes)
                .sum();

        model.addAttribute("fromDate", range[0]);
        model.addAttribute("toDate", range[1]);
        model.addAttribute("employeeCount", employees.size());
        model.addAttribute("administrativeCount", administrativeCount);
        model.addAttribute("serviceCount", serviceCount);
        model.addAttribute("averageAttendanceRate", round(averageAttendanceRate));
        model.addAttribute("insufficientCount", insufficientRows.size());
        model.addAttribute("totalOvertimeHours", totalOvertimeMinutes / 60);
        model.addAttribute("totalOvertimeRemainingMinutes", totalOvertimeMinutes % 60);
        model.addAttribute("totalLateHours", totalLateMinutes / 60);
        model.addAttribute("totalLateRemainingMinutes", totalLateMinutes % 60);
        model.addAttribute("totalEarlyLeaveHours", totalEarlyLeaveMinutes / 60);
        model.addAttribute("totalEarlyLeaveRemainingMinutes", totalEarlyLeaveMinutes % 60);

        return "admin-hr-statistics";
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

    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
