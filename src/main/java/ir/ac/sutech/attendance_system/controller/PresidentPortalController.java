package ir.ac.sutech.attendance_system.controller;

import ir.ac.sutech.attendance_system.dto.AttendanceSummaryReportRow;
import ir.ac.sutech.attendance_system.dto.DailyAttendanceReportRow;
import ir.ac.sutech.attendance_system.dto.DailyAttendanceStatus;
import ir.ac.sutech.attendance_system.dto.DepartmentPerformanceRow;
import ir.ac.sutech.attendance_system.model.Department;
import ir.ac.sutech.attendance_system.model.Employee;
import ir.ac.sutech.attendance_system.repository.DepartmentRepository;
import ir.ac.sutech.attendance_system.repository.EmployeeRepository;
import ir.ac.sutech.attendance_system.service.AttendanceSummaryExcelExporter;
import ir.ac.sutech.attendance_system.service.AttendanceSummaryReportService;
import ir.ac.sutech.attendance_system.service.DailyAttendanceReportService;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Controller
@RequestMapping("/president")
public class PresidentPortalController {

    private final DailyAttendanceReportService
            dailyAttendanceReportService;

    private final AttendanceSummaryReportService
            attendanceSummaryReportService;

    private final AttendanceSummaryExcelExporter
            attendanceSummaryExcelExporter;

    private final EmployeeRepository
            employeeRepository;

    private final DepartmentRepository
            departmentRepository;

    public PresidentPortalController(
            DailyAttendanceReportService dailyAttendanceReportService,
            AttendanceSummaryReportService attendanceSummaryReportService,
            AttendanceSummaryExcelExporter attendanceSummaryExcelExporter,
            EmployeeRepository employeeRepository,
            DepartmentRepository departmentRepository
    ) {

        this.dailyAttendanceReportService =
                dailyAttendanceReportService;

        this.attendanceSummaryReportService =
                attendanceSummaryReportService;

        this.attendanceSummaryExcelExporter =
                attendanceSummaryExcelExporter;

        this.employeeRepository =
                employeeRepository;

        this.departmentRepository =
                departmentRepository;
    }

    /*
     * ==========================================
     * ROOT
     * ==========================================
     */

    @GetMapping
    public String root() {

        return "redirect:/president/dashboard";
    }

    /*
     * ==========================================
     * UNIVERSITY DASHBOARD
     * ==========================================
     */

    @GetMapping("/dashboard")
    public String dashboard(
            Model model
    ) {

        LocalDate today =
                LocalDate.now();

        YearMonth currentMonth =
                YearMonth.from(today);

        LocalDate monthStart =
                currentMonth.atDay(1);

        List<DailyAttendanceReportRow> todayRows =
                List.of();

        List<AttendanceSummaryReportRow> monthRows =
                List.of();

        try {

            todayRows =
                    dailyAttendanceReportService
                            .generateReport(
                                    today,
                                    today,
                                    null
                            );

            monthRows =
                    attendanceSummaryReportService
                            .generateReport(
                                    monthStart,
                                    today,
                                    null
                            );

        } catch (
                IllegalArgumentException exception
        ) {

            model.addAttribute(
                    "errorMessage",
                    exception.getMessage()
            );
        }

        long presentToday =
                countStatus(
                        todayRows,
                        DailyAttendanceStatus.PRESENT
                );

        long absentToday =
                countStatus(
                        todayRows,
                        DailyAttendanceStatus.ABSENT
                );

        long incompleteToday =
                countStatus(
                        todayRows,
                        DailyAttendanceStatus.INCOMPLETE
                );

        long leaveToday =
                countStatus(
                        todayRows,
                        DailyAttendanceStatus.LEAVE
                );

        long missionToday =
                countStatus(
                        todayRows,
                        DailyAttendanceStatus.MISSION
                );

        long lateToday =
                todayRows
                        .stream()
                        .filter(
                                DailyAttendanceReportRow::isLate
                        )
                        .count();

        long todayDenominator =
                presentToday
                        +
                        absentToday
                        +
                        incompleteToday;

        double todayAttendanceRate =
                todayDenominator == 0
                        ? 0
                        : presentToday
                        *
                        100.0
                        /
                        todayDenominator;

        List<DepartmentPerformanceRow>
                departmentRows =
                buildDepartmentPerformance(
                        monthStart,
                        today
                );

        model.addAttribute(
                "today",
                today
        );

        model.addAttribute(
                "monthStart",
                monthStart
        );

        model.addAttribute(
                "activeEmployeeCount",
                employeeRepository
                        .findByActiveTrueOrderByLastNameAscFirstNameAsc()
                        .size()
        );

        model.addAttribute(
                "departmentCount",
                departmentRepository
                        .findAllByActiveTrueOrderByNameAsc()
                        .size()
        );

        model.addAttribute(
                "presentToday",
                presentToday
        );

        model.addAttribute(
                "absentToday",
                absentToday
        );

        model.addAttribute(
                "incompleteToday",
                incompleteToday
        );

        model.addAttribute(
                "leaveToday",
                leaveToday
        );

        model.addAttribute(
                "missionToday",
                missionToday
        );

        model.addAttribute(
                "lateToday",
                lateToday
        );

        model.addAttribute(
                "todayAttendanceRate",
                formatRate(
                        todayAttendanceRate
                )
        );

        model.addAttribute(
                "departmentRows",
                departmentRows
        );

        addSummaryStatistics(
                model,
                monthRows
        );

        return "president-dashboard";
    }

    /*
     * ==========================================
     * STATISTICAL / ANALYTICAL REPORT
     * ==========================================
     */

    @GetMapping("/reports")
    public String reports(
            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate fromDate,

            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate toDate,

            Model model
    ) {

        LocalDate today =
                LocalDate.now();

        if (fromDate == null) {

            fromDate =
                    YearMonth
                            .from(today)
                            .atDay(1);
        }

        if (toDate == null) {
            toDate = today;
        }

        List<AttendanceSummaryReportRow> rows =
                List.of();

        try {

            validateDateRange(
                    fromDate,
                    toDate
            );

            rows =
                    attendanceSummaryReportService
                            .generateReport(
                                    fromDate,
                                    toDate,
                                    null
                            );

        } catch (
                IllegalArgumentException exception
        ) {

            model.addAttribute(
                    "errorMessage",
                    exception.getMessage()
            );
        }

        model.addAttribute(
                "fromDate",
                fromDate
        );

        model.addAttribute(
                "toDate",
                toDate
        );

        model.addAttribute(
                "summaryRows",
                rows
        );

        model.addAttribute(
                "employeeCount",
                rows.size()
        );

        addSummaryStatistics(
                model,
                rows
        );

        return "president-reports";
    }

    @GetMapping("/reports/excel")
    public ResponseEntity<byte[]> reportsExcel(
            @RequestParam
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate fromDate,

            @RequestParam
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate toDate
    ) throws IOException {

        validateDateRange(
                fromDate,
                toDate
        );

        List<AttendanceSummaryReportRow> rows =
                attendanceSummaryReportService
                        .generateReport(
                                fromDate,
                                toDate,
                                null
                        );

        byte[] file =
                attendanceSummaryExcelExporter
                        .export(
                                rows,
                                fromDate,
                                toDate
                        );

        String fileName =
                "president-university-attendance-"
                        +
                        fromDate
                        +
                        "-to-"
                        +
                        toDate
                        +
                        ".xlsx";

        return ResponseEntity
                .ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\""
                                +
                                fileName
                                +
                                "\""
                )
                .contentType(
                        MediaType.parseMediaType(
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                        )
                )
                .contentLength(
                        file.length
                )
                .body(file);
    }

    /*
     * ==========================================
     * DEPARTMENT COMPARISON
     * ==========================================
     */

    @GetMapping("/departments")
    public String departments(
            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate fromDate,

            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate toDate,

            Model model
    ) {

        LocalDate today =
                LocalDate.now();

        if (fromDate == null) {

            fromDate =
                    YearMonth
                            .from(today)
                            .atDay(1);
        }

        if (toDate == null) {
            toDate = today;
        }

        List<DepartmentPerformanceRow> rows =
                List.of();

        try {

            validateDateRange(
                    fromDate,
                    toDate
            );

            rows =
                    buildDepartmentPerformance(
                            fromDate,
                            toDate
                    );

        } catch (
                IllegalArgumentException exception
        ) {

            model.addAttribute(
                    "errorMessage",
                    exception.getMessage()
            );
        }

        model.addAttribute(
                "fromDate",
                fromDate
        );

        model.addAttribute(
                "toDate",
                toDate
        );

        model.addAttribute(
                "departmentRows",
                rows
        );

        model.addAttribute(
                "departmentCount",
                rows.size()
        );

        return "president-departments";
    }

    /*
     * ==========================================
     * KPI
     * ==========================================
     */

    @GetMapping("/kpi")
    public String kpi(
            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate fromDate,

            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate toDate,

            Model model
    ) {

        LocalDate today =
                LocalDate.now();

        if (fromDate == null) {

            fromDate =
                    YearMonth
                            .from(today)
                            .atDay(1);
        }

        if (toDate == null) {
            toDate = today;
        }

        List<AttendanceSummaryReportRow> rows =
                List.of();

        List<DepartmentPerformanceRow> departmentRows =
                List.of();

        try {

            validateDateRange(
                    fromDate,
                    toDate
            );

            rows =
                    attendanceSummaryReportService
                            .generateReport(
                                    fromDate,
                                    toDate,
                                    null
                            );

            departmentRows =
                    buildDepartmentPerformance(
                            fromDate,
                            toDate
                    );

        } catch (
                IllegalArgumentException exception
        ) {

            model.addAttribute(
                    "errorMessage",
                    exception.getMessage()
            );
        }

        long presentDays =
                rows.stream()
                        .mapToLong(
                                AttendanceSummaryReportRow::getPresentDays
                        )
                        .sum();

        long absentDays =
                rows.stream()
                        .mapToLong(
                                AttendanceSummaryReportRow::getAbsentDays
                        )
                        .sum();

        long incompleteDays =
                rows.stream()
                        .mapToLong(
                                AttendanceSummaryReportRow::getIncompleteDays
                        )
                        .sum();

        long totalWorkedMinutes =
                rows.stream()
                        .mapToLong(
                                AttendanceSummaryReportRow::getTotalWorkedMinutes
                        )
                        .sum();

        long totalLateMinutes =
                rows.stream()
                        .mapToLong(
                                AttendanceSummaryReportRow::getTotalLateMinutes
                        )
                        .sum();

        long totalEarlyLeaveMinutes =
                rows.stream()
                        .mapToLong(
                                AttendanceSummaryReportRow::getTotalEarlyLeaveMinutes
                        )
                        .sum();

        long evaluationDays =
                presentDays
                        +
                        absentDays
                        +
                        incompleteDays;

        double attendanceRate =
                evaluationDays == 0
                        ? 0
                        : presentDays
                        *
                        100.0
                        /
                        evaluationDays;

        double absenceRate =
                evaluationDays == 0
                        ? 0
                        : absentDays
                        *
                        100.0
                        /
                        evaluationDays;

        double incompleteRate =
                evaluationDays == 0
                        ? 0
                        : incompleteDays
                        *
                        100.0
                        /
                        evaluationDays;

        double averageWorkedHours =
                presentDays == 0
                        ? 0
                        : totalWorkedMinutes
                        /
                        60.0
                        /
                        presentDays;

        double averageLateMinutes =
                presentDays == 0
                        ? 0
                        : totalLateMinutes
                        *
                        1.0
                        /
                        presentDays;

        double averageEarlyLeaveMinutes =
                presentDays == 0
                        ? 0
                        : totalEarlyLeaveMinutes
                        *
                        1.0
                        /
                        presentDays;

        model.addAttribute(
                "fromDate",
                fromDate
        );

        model.addAttribute(
                "toDate",
                toDate
        );

        model.addAttribute(
                "employeeCount",
                rows.size()
        );

        model.addAttribute(
                "evaluationDays",
                evaluationDays
        );

        model.addAttribute(
                "attendanceRate",
                formatRate(
                        attendanceRate
                )
        );

        model.addAttribute(
                "absenceRate",
                formatRate(
                        absenceRate
                )
        );

        model.addAttribute(
                "incompleteRate",
                formatRate(
                        incompleteRate
                )
        );

        model.addAttribute(
                "averageWorkedHours",
                formatRate(
                        averageWorkedHours
                )
        );

        model.addAttribute(
                "averageLateMinutes",
                formatRate(
                        averageLateMinutes
                )
        );

        model.addAttribute(
                "averageEarlyLeaveMinutes",
                formatRate(
                        averageEarlyLeaveMinutes
                )
        );

        model.addAttribute(
                "departmentRows",
                departmentRows
        );

        return "president-kpi";
    }

    /*
     * ==========================================
     * BUILD DEPARTMENT PERFORMANCE
     * ==========================================
     */

    private List<DepartmentPerformanceRow>
    buildDepartmentPerformance(
            LocalDate fromDate,
            LocalDate toDate
    ) {

        List<Department> departments =
                departmentRepository
                        .findAllByActiveTrueOrderByNameAsc();

        List<Employee> employees =
                employeeRepository
                        .findByActiveTrueOrderByLastNameAscFirstNameAsc();

        List<AttendanceSummaryReportRow> allRows =
                attendanceSummaryReportService
                        .generateReport(
                                fromDate,
                                toDate,
                                null
                        );

        List<DepartmentPerformanceRow> result =
                new ArrayList<>();

        for (
                Department department
                :
                departments
        ) {

            Long departmentId =
                    department.getId();

            long employeeCount =
                    employees
                            .stream()
                            .filter(employee ->
                                    employee.getDepartment()
                                            != null
                                            &&
                                    departmentId.equals(
                                            employee
                                                .getDepartment()
                                                .getId()
                                    )
                            )
                            .count();

            List<AttendanceSummaryReportRow>
                    departmentRows =
                    allRows
                            .stream()
                            .filter(row ->
                                    row.getEmployee()
                                            != null
                                            &&
                                    row.getEmployee()
                                            .getDepartment()
                                            != null
                                            &&
                                    departmentId.equals(
                                            row.getEmployee()
                                                .getDepartment()
                                                .getId()
                                    )
                            )
                            .toList();

            long presentDays =
                    departmentRows
                            .stream()
                            .mapToLong(
                                    AttendanceSummaryReportRow::getPresentDays
                            )
                            .sum();

            long absentDays =
                    departmentRows
                            .stream()
                            .mapToLong(
                                    AttendanceSummaryReportRow::getAbsentDays
                            )
                            .sum();

            long incompleteDays =
                    departmentRows
                            .stream()
                            .mapToLong(
                                    AttendanceSummaryReportRow::getIncompleteDays
                            )
                            .sum();

            long leaveDays =
                    departmentRows
                            .stream()
                            .mapToLong(
                                    AttendanceSummaryReportRow::getLeaveDays
                            )
                            .sum();

            long missionDays =
                    departmentRows
                            .stream()
                            .mapToLong(
                                    AttendanceSummaryReportRow::getMissionDays
                            )
                            .sum();

            long workedMinutes =
                    departmentRows
                            .stream()
                            .mapToLong(
                                    AttendanceSummaryReportRow::getTotalWorkedMinutes
                            )
                            .sum();

            long lateMinutes =
                    departmentRows
                            .stream()
                            .mapToLong(
                                    AttendanceSummaryReportRow::getTotalLateMinutes
                            )
                            .sum();

            long earlyLeaveMinutes =
                    departmentRows
                            .stream()
                            .mapToLong(
                                    AttendanceSummaryReportRow::getTotalEarlyLeaveMinutes
                            )
                            .sum();

            result.add(
                    new DepartmentPerformanceRow(
                            departmentId,
                            department.getCode(),
                            department.getName(),
                            employeeCount,
                            presentDays,
                            absentDays,
                            incompleteDays,
                            leaveDays,
                            missionDays,
                            workedMinutes,
                            lateMinutes,
                            earlyLeaveMinutes
                    )
            );
        }

        return result;
    }

    /*
     * ==========================================
     * SUMMARY STATISTICS
     * ==========================================
     */

    private void addSummaryStatistics(
            Model model,
            List<AttendanceSummaryReportRow> rows
    ) {

        long presentDays =
                rows.stream()
                        .mapToLong(
                                AttendanceSummaryReportRow::getPresentDays
                        )
                        .sum();

        long absentDays =
                rows.stream()
                        .mapToLong(
                                AttendanceSummaryReportRow::getAbsentDays
                        )
                        .sum();

        long incompleteDays =
                rows.stream()
                        .mapToLong(
                                AttendanceSummaryReportRow::getIncompleteDays
                        )
                        .sum();

        long leaveDays =
                rows.stream()
                        .mapToLong(
                                AttendanceSummaryReportRow::getLeaveDays
                        )
                        .sum();

        long missionDays =
                rows.stream()
                        .mapToLong(
                                AttendanceSummaryReportRow::getMissionDays
                        )
                        .sum();

        long holidayDays =
                rows.stream()
                        .mapToLong(
                                AttendanceSummaryReportRow::getHolidayDays
                        )
                        .sum();

        long noShiftDays =
                rows.stream()
                        .mapToLong(
                                AttendanceSummaryReportRow::getNoShiftDays
                        )
                        .sum();

        long workedMinutes =
                rows.stream()
                        .mapToLong(
                                AttendanceSummaryReportRow::getTotalWorkedMinutes
                        )
                        .sum();

        long lateMinutes =
                rows.stream()
                        .mapToLong(
                                AttendanceSummaryReportRow::getTotalLateMinutes
                        )
                        .sum();

        long earlyLeaveMinutes =
                rows.stream()
                        .mapToLong(
                                AttendanceSummaryReportRow::getTotalEarlyLeaveMinutes
                        )
                        .sum();

        long denominator =
                presentDays
                        +
                        absentDays
                        +
                        incompleteDays;

        double attendanceRate =
                denominator == 0
                        ? 0
                        : presentDays
                        *
                        100.0
                        /
                        denominator;

        model.addAttribute(
                "totalPresentDays",
                presentDays
        );

        model.addAttribute(
                "totalAbsentDays",
                absentDays
        );

        model.addAttribute(
                "totalIncompleteDays",
                incompleteDays
        );

        model.addAttribute(
                "totalLeaveDays",
                leaveDays
        );

        model.addAttribute(
                "totalMissionDays",
                missionDays
        );

        model.addAttribute(
                "totalHolidayDays",
                holidayDays
        );

        model.addAttribute(
                "totalNoShiftDays",
                noShiftDays
        );

        model.addAttribute(
                "totalWorkedHours",
                workedMinutes / 60
        );

        model.addAttribute(
                "totalWorkedRemainingMinutes",
                workedMinutes % 60
        );

        model.addAttribute(
                "totalLateHours",
                lateMinutes / 60
        );

        model.addAttribute(
                "totalLateRemainingMinutes",
                lateMinutes % 60
        );

        model.addAttribute(
                "totalEarlyLeaveHours",
                earlyLeaveMinutes / 60
        );

        model.addAttribute(
                "totalEarlyLeaveRemainingMinutes",
                earlyLeaveMinutes % 60
        );

        model.addAttribute(
                "attendanceRate",
                formatRate(
                        attendanceRate
                )
        );
    }

    private long countStatus(
            List<DailyAttendanceReportRow> rows,
            DailyAttendanceStatus status
    ) {

        return rows.stream()
                .filter(row ->
                        row.getStatus()
                                ==
                                status
                )
                .count();
    }

    private String formatRate(
            double value
    ) {

        return String.format(
                Locale.ROOT,
                "%.1f",
                value
        );
    }

    private void validateDateRange(
            LocalDate fromDate,
            LocalDate toDate
    ) {

        if (
                fromDate == null
                        ||
                        toDate == null
        ) {
            throw new IllegalArgumentException(
                    "تاریخ شروع و پایان الزامی است."
            );
        }

        if (
                toDate.isBefore(
                        fromDate
                )
        ) {
            throw new IllegalArgumentException(
                    "تاریخ پایان نمی‌تواند قبل از تاریخ شروع باشد."
            );
        }

        if (
                fromDate.plusYears(2)
                        .isBefore(
                                toDate
                        )
        ) {
            throw new IllegalArgumentException(
                    "بازه گزارش نمی‌تواند بیشتر از دو سال باشد."
            );
        }
    }
}
