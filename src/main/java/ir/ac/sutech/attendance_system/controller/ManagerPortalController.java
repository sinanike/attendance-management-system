package ir.ac.sutech.attendance_system.controller;

import ir.ac.sutech.attendance_system.dto.AttendanceSummaryReportRow;
import ir.ac.sutech.attendance_system.dto.DailyAttendanceReportRow;
import ir.ac.sutech.attendance_system.dto.DailyAttendanceStatus;

import ir.ac.sutech.attendance_system.model.Employee;
import ir.ac.sutech.attendance_system.model.LeaveRequest;
import ir.ac.sutech.attendance_system.model.LeaveStatus;
import ir.ac.sutech.attendance_system.model.MissionRequest;
import ir.ac.sutech.attendance_system.model.MissionStatus;

import ir.ac.sutech.attendance_system.repository.EmployeeRepository;
import ir.ac.sutech.attendance_system.repository.LeaveRequestRepository;
import ir.ac.sutech.attendance_system.repository.MissionRequestRepository;

import ir.ac.sutech.attendance_system.service.AttendanceSummaryExcelExporter;
import ir.ac.sutech.attendance_system.service.AttendanceSummaryReportService;
import ir.ac.sutech.attendance_system.service.CurrentManagerService;
import ir.ac.sutech.attendance_system.service.DailyAttendanceReportService;

import org.springframework.format.annotation.DateTimeFormat;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.Authentication;

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
@RequestMapping("/manager")
public class ManagerPortalController {


    private final CurrentManagerService
            currentManagerService;


    private final EmployeeRepository
            employeeRepository;


    private final LeaveRequestRepository
            leaveRequestRepository;


    private final MissionRequestRepository
            missionRequestRepository;


    private final DailyAttendanceReportService
            dailyAttendanceReportService;


    private final AttendanceSummaryReportService
            attendanceSummaryReportService;


    private final AttendanceSummaryExcelExporter
            attendanceSummaryExcelExporter;


    public ManagerPortalController(

            CurrentManagerService currentManagerService,

            EmployeeRepository employeeRepository,

            LeaveRequestRepository leaveRequestRepository,

            MissionRequestRepository missionRequestRepository,

            DailyAttendanceReportService dailyAttendanceReportService,

            AttendanceSummaryReportService attendanceSummaryReportService,

            AttendanceSummaryExcelExporter attendanceSummaryExcelExporter

    ) {


        this.currentManagerService =
                currentManagerService;


        this.employeeRepository =
                employeeRepository;


        this.leaveRequestRepository =
                leaveRequestRepository;


        this.missionRequestRepository =
                missionRequestRepository;


        this.dailyAttendanceReportService =
                dailyAttendanceReportService;


        this.attendanceSummaryReportService =
                attendanceSummaryReportService;


        this.attendanceSummaryExcelExporter =
                attendanceSummaryExcelExporter;
    }


    /*
     * ==========================================
     * ریشه پنل Manager
     * ==========================================
     */

    @GetMapping
    public String root() {

        return "redirect:/manager/dashboard";
    }


    /*
     * ==========================================
     * Dashboard
     * ==========================================
     */

    @GetMapping("/dashboard")
    public String dashboard(

            Authentication authentication,

            Model model

    ) {


        Employee manager =
                currentManagerService
                        .getCurrentManager(
                                authentication
                        );


        Long departmentId =
                manager
                        .getDepartment()
                        .getId();


        /*
         * ======================================
         * کارکنان فعال واحد
         * ======================================
         */

        List<Employee> employees =
                employeeRepository
                        .findByDepartmentIdAndActiveTrueOrderByLastNameAscFirstNameAsc(
                                departmentId
                        );


        LocalDate today =
                LocalDate.now();


        /*
         * ======================================
         * گزارش امروز
         * ======================================
         */

        List<DailyAttendanceReportRow> todayRows =
                generateReportForEmployees(

                        employees,

                        today,

                        today
                );


        /*
         * ======================================
         * وضعیت امروز
         * ======================================
         */

        long presentCount =
                countStatus(

                        todayRows,

                        DailyAttendanceStatus.PRESENT
                );


        long absentCount =
                countStatus(

                        todayRows,

                        DailyAttendanceStatus.ABSENT
                );


        long incompleteCount =
                countStatus(

                        todayRows,

                        DailyAttendanceStatus.INCOMPLETE
                );


        long leaveCount =
                countStatus(

                        todayRows,

                        DailyAttendanceStatus.LEAVE
                );


        long missionCount =
                countStatus(

                        todayRows,

                        DailyAttendanceStatus.MISSION
                );


        long holidayCount =
                countStatus(

                        todayRows,

                        DailyAttendanceStatus.HOLIDAY
                );


        long noShiftCount =
                countStatus(

                        todayRows,

                        DailyAttendanceStatus.NO_SHIFT
                );


        long lateCount =
                todayRows
                        .stream()
                        .filter(
                                DailyAttendanceReportRow::isLate
                        )
                        .count();


        /*
         * ======================================
         * نرخ حضور
         * ======================================
         */

        long attendanceDenominator =
                presentCount
                        +
                        absentCount
                        +
                        incompleteCount;


        double attendanceRate =
                attendanceDenominator == 0

                        ? 0

                        : presentCount
                        * 100.0
                        / attendanceDenominator;


        String attendanceRateText =
                String.format(

                        Locale.ROOT,

                        "%.1f",

                        attendanceRate
                );


        /*
         * ======================================
         * درخواست‌های Pending واحد
         *
         * درخواست شخصی Manager
         * در Dashboard مدیریت نمی‌شود.
         * ======================================
         */

        long pendingLeaveCount =
                leaveRequestRepository
                        .countByEmployeeDepartmentIdAndStatusAndEmployeeIdNot(

                                departmentId,

                                LeaveStatus.PENDING,

                                manager.getId()
                        );


        long pendingMissionCount =
                missionRequestRepository
                        .countByEmployeeDepartmentIdAndStatusAndEmployeeIdNot(

                                departmentId,

                                MissionStatus.PENDING,

                                manager.getId()
                        );


        long pendingRequestCount =
                pendingLeaveCount
                        +
                        pendingMissionCount;


        /*
         * ======================================
         * 5 مرخصی Pending اخیر
         * ======================================
         */

        List<LeaveRequest> recentPendingLeaveRequests =
                leaveRequestRepository
                        .findTop5ByEmployeeDepartmentIdAndStatusAndEmployeeIdNotOrderByCreatedAtDescIdDesc(

                                departmentId,

                                LeaveStatus.PENDING,

                                manager.getId()
                        );


        /*
         * ======================================
         * 5 مأموریت Pending اخیر
         * ======================================
         */

        List<MissionRequest> recentPendingMissionRequests =
                missionRequestRepository
                        .findTop5ByEmployeeDepartmentIdAndStatusAndEmployeeIdNotOrderByCreatedAtDescIdDesc(

                                departmentId,

                                MissionStatus.PENDING,

                                manager.getId()
                        );


        /*
         * ======================================
         * Model
         * ======================================
         */

        model.addAttribute(
                "manager",
                manager
        );


        model.addAttribute(
                "department",
                manager.getDepartment()
        );


        model.addAttribute(
                "today",
                today
        );


        model.addAttribute(
                "employees",
                employees
        );


        model.addAttribute(
                "totalEmployees",
                employees.size()
        );


        model.addAttribute(
                "presentCount",
                presentCount
        );


        model.addAttribute(
                "absentCount",
                absentCount
        );


        model.addAttribute(
                "incompleteCount",
                incompleteCount
        );


        model.addAttribute(
                "leaveCount",
                leaveCount
        );


        model.addAttribute(
                "missionCount",
                missionCount
        );


        model.addAttribute(
                "holidayCount",
                holidayCount
        );


        model.addAttribute(
                "noShiftCount",
                noShiftCount
        );


        model.addAttribute(
                "lateCount",
                lateCount
        );


        model.addAttribute(
                "attendanceRate",
                attendanceRateText
        );


        model.addAttribute(
                "todayRows",
                todayRows
        );


        /*
         * Pending Requests
         */

        model.addAttribute(
                "pendingLeaveCount",
                pendingLeaveCount
        );


        model.addAttribute(
                "pendingMissionCount",
                pendingMissionCount
        );


        model.addAttribute(
                "pendingRequestCount",
                pendingRequestCount
        );


        model.addAttribute(
                "recentPendingLeaveRequests",
                recentPendingLeaveRequests
        );


        model.addAttribute(
                "recentPendingMissionRequests",
                recentPendingMissionRequests
        );


        return "manager-dashboard";
    }


    /*
     * ==========================================
     * کارکنان واحد من
     * ==========================================
     */

    @GetMapping("/employees")
    public String employees(

            Authentication authentication,

            Model model

    ) {


        Employee manager =
                currentManagerService
                        .getCurrentManager(
                                authentication
                        );


        Long departmentId =
                manager
                        .getDepartment()
                        .getId();


        List<Employee> employees =
                employeeRepository
                        .findByDepartmentIdOrderByLastNameAscFirstNameAsc(
                                departmentId
                        );


        long activeCount =
                employees
                        .stream()
                        .filter(
                                Employee::isActive
                        )
                        .count();


        long inactiveCount =
                employees.size()
                        -
                        activeCount;


        model.addAttribute(
                "manager",
                manager
        );


        model.addAttribute(
                "department",
                manager.getDepartment()
        );


        model.addAttribute(
                "employees",
                employees
        );


        model.addAttribute(
                "totalCount",
                employees.size()
        );


        model.addAttribute(
                "activeCount",
                activeCount
        );


        model.addAttribute(
                "inactiveCount",
                inactiveCount
        );


        return "manager-employees";
    }


    /*
     * ==========================================
     * گزارش حضور و غیاب
     * ==========================================
     */

    @GetMapping("/attendance")
    public String attendance(

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


            @RequestParam(required = false)
            Long employeeId,


            Authentication authentication,

            Model model

    ) {


        Employee manager =
                currentManagerService
                        .getCurrentManager(
                                authentication
                        );


        Long departmentId =
                manager
                        .getDepartment()
                        .getId();


        List<Employee> departmentEmployees =
                employeeRepository
                        .findByDepartmentIdAndActiveTrueOrderByLastNameAscFirstNameAsc(
                                departmentId
                        );


        LocalDate today =
                LocalDate.now();


        /*
         * پیش‌فرض ماه جاری
         */

        if (fromDate == null) {

            fromDate =
                    YearMonth
                            .from(today)
                            .atDay(1);
        }


        if (toDate == null) {

            toDate =
                    today;
        }


        List<DailyAttendanceReportRow> reportRows =
                List.of();


        Employee selectedEmployee =
                null;


        try {


            validateDateRange(
                    fromDate,
                    toDate
            );


            selectedEmployee =
                    resolveSelectedEmployee(

                            departmentEmployees,

                            employeeId
                    );


            List<Employee> employeesForReport;


            if (selectedEmployee != null) {


                employeesForReport =
                        List.of(
                                selectedEmployee
                        );


            } else {


                employeesForReport =
                        departmentEmployees;
            }


            reportRows =
                    generateReportForEmployees(

                            employeesForReport,

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


        /*
         * ======================================
         * وضعیت‌ها
         * ======================================
         */

        long presentCount =
                countStatus(

                        reportRows,

                        DailyAttendanceStatus.PRESENT
                );


        long absentCount =
                countStatus(

                        reportRows,

                        DailyAttendanceStatus.ABSENT
                );


        long incompleteCount =
                countStatus(

                        reportRows,

                        DailyAttendanceStatus.INCOMPLETE
                );


        long leaveCount =
                countStatus(

                        reportRows,

                        DailyAttendanceStatus.LEAVE
                );


        long missionCount =
                countStatus(

                        reportRows,

                        DailyAttendanceStatus.MISSION
                );


        long holidayCount =
                countStatus(

                        reportRows,

                        DailyAttendanceStatus.HOLIDAY
                );


        long noShiftCount =
                countStatus(

                        reportRows,

                        DailyAttendanceStatus.NO_SHIFT
                );


        long lateCount =
                reportRows
                        .stream()
                        .filter(
                                DailyAttendanceReportRow::isLate
                        )
                        .count();


        long earlyLeaveCount =
                reportRows
                        .stream()
                        .filter(
                                DailyAttendanceReportRow::isEarlyLeave
                        )
                        .count();


        /*
         * ======================================
         * زمان‌ها
         * ======================================
         */

        long workedMinutes =
                reportRows
                        .stream()
                        .mapToLong(
                                DailyAttendanceReportRow
                                        ::getWorkedMinutes
                        )
                        .sum();


        long lateMinutes =
                reportRows
                        .stream()
                        .mapToLong(
                                DailyAttendanceReportRow
                                        ::getLateMinutes
                        )
                        .sum();


        long earlyLeaveMinutes =
                reportRows
                        .stream()
                        .mapToLong(
                                DailyAttendanceReportRow
                                        ::getEarlyLeaveMinutes
                        )
                        .sum();


        /*
         * ======================================
         * Model
         * ======================================
         */

        model.addAttribute(
                "manager",
                manager
        );


        model.addAttribute(
                "department",
                manager.getDepartment()
        );


        model.addAttribute(
                "employees",
                departmentEmployees
        );


        model.addAttribute(
                "selectedEmployee",
                selectedEmployee
        );


        model.addAttribute(
                "selectedEmployeeId",
                employeeId
        );


        model.addAttribute(
                "fromDate",
                fromDate
        );


        model.addAttribute(
                "toDate",
                toDate
        );


        model.addAttribute(
                "reportRows",
                reportRows
        );


        model.addAttribute(
                "rowCount",
                reportRows.size()
        );


        model.addAttribute(
                "presentCount",
                presentCount
        );


        model.addAttribute(
                "absentCount",
                absentCount
        );


        model.addAttribute(
                "incompleteCount",
                incompleteCount
        );


        model.addAttribute(
                "leaveCount",
                leaveCount
        );


        model.addAttribute(
                "missionCount",
                missionCount
        );


        model.addAttribute(
                "holidayCount",
                holidayCount
        );


        model.addAttribute(
                "noShiftCount",
                noShiftCount
        );


        model.addAttribute(
                "lateCount",
                lateCount
        );


        model.addAttribute(
                "earlyLeaveCount",
                earlyLeaveCount
        );


        model.addAttribute(
                "workedHours",
                workedMinutes / 60
        );


        model.addAttribute(
                "workedRemainingMinutes",
                workedMinutes % 60
        );


        model.addAttribute(
                "lateHours",
                lateMinutes / 60
        );


        model.addAttribute(
                "lateRemainingMinutes",
                lateMinutes % 60
        );


        model.addAttribute(
                "earlyLeaveHours",
                earlyLeaveMinutes / 60
        );


        model.addAttribute(
                "earlyLeaveRemainingMinutes",
                earlyLeaveMinutes % 60
        );


        return "manager-attendance";
    }


    /*
     * ==========================================
     * گزارش خلاصه
     * ==========================================
     */

    @GetMapping("/summary")
    public String summary(

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


            @RequestParam(required = false)
            Long employeeId,


            Authentication authentication,

            Model model

    ) {


        Employee manager =
                currentManagerService
                        .getCurrentManager(
                                authentication
                        );


        Long departmentId =
                manager
                        .getDepartment()
                        .getId();


        List<Employee> departmentEmployees =
                employeeRepository
                        .findByDepartmentIdAndActiveTrueOrderByLastNameAscFirstNameAsc(
                                departmentId
                        );


        LocalDate today =
                LocalDate.now();


        if (fromDate == null) {

            fromDate =
                    YearMonth
                            .from(today)
                            .atDay(1);
        }


        if (toDate == null) {

            toDate =
                    today;
        }


        List<AttendanceSummaryReportRow> summaryRows =
                List.of();


        Employee selectedEmployee =
                null;


        try {


            validateDateRange(
                    fromDate,
                    toDate
            );


            selectedEmployee =
                    resolveSelectedEmployee(

                            departmentEmployees,

                            employeeId
                    );


            List<Employee> employeesForReport;


            if (selectedEmployee != null) {


                employeesForReport =
                        List.of(
                                selectedEmployee
                        );


            } else {


                employeesForReport =
                        departmentEmployees;
            }


            summaryRows =
                    generateSummaryForEmployees(

                            employeesForReport,

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


        /*
         * ======================================
         * نفر-روز
         * ======================================
         */

        long totalPresentDays =
                summaryRows
                        .stream()
                        .mapToLong(
                                AttendanceSummaryReportRow
                                        ::getPresentDays
                        )
                        .sum();


        long totalAbsentDays =
                summaryRows
                        .stream()
                        .mapToLong(
                                AttendanceSummaryReportRow
                                        ::getAbsentDays
                        )
                        .sum();


        long totalIncompleteDays =
                summaryRows
                        .stream()
                        .mapToLong(
                                AttendanceSummaryReportRow
                                        ::getIncompleteDays
                        )
                        .sum();


        long totalLeaveDays =
                summaryRows
                        .stream()
                        .mapToLong(
                                AttendanceSummaryReportRow
                                        ::getLeaveDays
                        )
                        .sum();


        long totalMissionDays =
                summaryRows
                        .stream()
                        .mapToLong(
                                AttendanceSummaryReportRow
                                        ::getMissionDays
                        )
                        .sum();


        long totalHolidayDays =
                summaryRows
                        .stream()
                        .mapToLong(
                                AttendanceSummaryReportRow
                                        ::getHolidayDays
                        )
                        .sum();


        long totalNoShiftDays =
                summaryRows
                        .stream()
                        .mapToLong(
                                AttendanceSummaryReportRow
                                        ::getNoShiftDays
                        )
                        .sum();


        /*
         * ======================================
         * زمان‌ها
         * ======================================
         */

        long totalWorkedMinutes =
                summaryRows
                        .stream()
                        .mapToLong(
                                AttendanceSummaryReportRow
                                        ::getTotalWorkedMinutes
                        )
                        .sum();


        long totalLateMinutes =
                summaryRows
                        .stream()
                        .mapToLong(
                                AttendanceSummaryReportRow
                                        ::getTotalLateMinutes
                        )
                        .sum();


        long totalEarlyLeaveMinutes =
                summaryRows
                        .stream()
                        .mapToLong(
                                AttendanceSummaryReportRow
                                        ::getTotalEarlyLeaveMinutes
                        )
                        .sum();


        /*
         * ======================================
         * نرخ حضور
         * ======================================
         */

        long attendanceDenominator =
                totalPresentDays
                        +
                        totalAbsentDays
                        +
                        totalIncompleteDays;


        double attendanceRate =
                attendanceDenominator == 0

                        ? 0

                        : totalPresentDays
                        * 100.0
                        / attendanceDenominator;


        String attendanceRateText =
                String.format(

                        Locale.ROOT,

                        "%.1f",

                        attendanceRate
                );


        /*
         * ======================================
         * Model
         * ======================================
         */

        model.addAttribute(
                "manager",
                manager
        );


        model.addAttribute(
                "department",
                manager.getDepartment()
        );


        model.addAttribute(
                "employees",
                departmentEmployees
        );


        model.addAttribute(
                "selectedEmployee",
                selectedEmployee
        );


        model.addAttribute(
                "selectedEmployeeId",
                employeeId
        );


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
                summaryRows
        );


        model.addAttribute(
                "employeeCount",
                summaryRows.size()
        );


        model.addAttribute(
                "totalPresentDays",
                totalPresentDays
        );


        model.addAttribute(
                "totalAbsentDays",
                totalAbsentDays
        );


        model.addAttribute(
                "totalIncompleteDays",
                totalIncompleteDays
        );


        model.addAttribute(
                "totalLeaveDays",
                totalLeaveDays
        );


        model.addAttribute(
                "totalMissionDays",
                totalMissionDays
        );


        model.addAttribute(
                "totalHolidayDays",
                totalHolidayDays
        );


        model.addAttribute(
                "totalNoShiftDays",
                totalNoShiftDays
        );


        model.addAttribute(
                "totalWorkedHours",
                totalWorkedMinutes / 60
        );


        model.addAttribute(
                "totalWorkedRemainingMinutes",
                totalWorkedMinutes % 60
        );


        model.addAttribute(
                "totalLateHours",
                totalLateMinutes / 60
        );


        model.addAttribute(
                "totalLateRemainingMinutes",
                totalLateMinutes % 60
        );


        model.addAttribute(
                "totalEarlyLeaveHours",
                totalEarlyLeaveMinutes / 60
        );


        model.addAttribute(
                "totalEarlyLeaveRemainingMinutes",
                totalEarlyLeaveMinutes % 60
        );


        model.addAttribute(
                "attendanceRate",
                attendanceRateText
        );


        return "manager-summary";
    }


    /*
     * ==========================================
     * Excel خلاصه Manager
     * ==========================================
     */

    @GetMapping("/summary/excel")
    public ResponseEntity<byte[]> summaryExcel(

            @RequestParam
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate fromDate,


            @RequestParam
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate toDate,


            @RequestParam(required = false)
            Long employeeId,


            Authentication authentication

    ) throws IOException {


        Employee manager =
                currentManagerService
                        .getCurrentManager(
                                authentication
                        );


        Long departmentId =
                manager
                        .getDepartment()
                        .getId();


        List<Employee> departmentEmployees =
                employeeRepository
                        .findByDepartmentIdAndActiveTrueOrderByLastNameAscFirstNameAsc(
                                departmentId
                        );


        validateDateRange(
                fromDate,
                toDate
        );


        Employee selectedEmployee =
                resolveSelectedEmployee(

                        departmentEmployees,

                        employeeId
                );


        List<Employee> employeesForReport;


        if (selectedEmployee != null) {


            employeesForReport =
                    List.of(
                            selectedEmployee
                    );


        } else {


            employeesForReport =
                    departmentEmployees;
        }


        List<AttendanceSummaryReportRow> summaryRows =
                generateSummaryForEmployees(

                        employeesForReport,

                        fromDate,

                        toDate
                );


        byte[] excelFile =
                attendanceSummaryExcelExporter
                        .export(

                                summaryRows,

                                fromDate,

                                toDate
                        );


        String fileName =
                "manager-attendance-summary-"
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
                        excelFile.length
                )

                .body(
                        excelFile
                );
    }


    /*
     * ==========================================
     * بررسی Employee انتخاب شده
     *
     * فقط Department مدیر
     * ==========================================
     */

    private Employee resolveSelectedEmployee(

            List<Employee> departmentEmployees,

            Long employeeId

    ) {


        if (employeeId == null) {

            return null;
        }


        return departmentEmployees
                .stream()

                .filter(employee ->
                        employeeId.equals(
                                employee.getId()
                        )
                )

                .findFirst()

                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "کارمند انتخاب‌شده در واحد سازمانی شما قرار ندارد."
                        )
                );
    }


    /*
     * ==========================================
     * تولید Daily Report کارکنان
     * ==========================================
     */

    private List<DailyAttendanceReportRow>
    generateReportForEmployees(

            List<Employee> employees,

            LocalDate fromDate,

            LocalDate toDate

    ) {


        List<DailyAttendanceReportRow> rows =
                new ArrayList<>();


        for (Employee employee : employees) {


            rows.addAll(

                    dailyAttendanceReportService
                            .generateReport(

                                    fromDate,

                                    toDate,

                                    employee.getId()
                            )
            );
        }


        rows.sort(

                (first, second) -> {


                    int dateCompare =
                            second
                                    .getReportDate()
                                    .compareTo(
                                            first.getReportDate()
                                    );


                    if (dateCompare != 0) {

                        return dateCompare;
                    }


                    String firstLastName =
                            first
                                    .getEmployee()
                                    .getLastName()
                                    == null

                                    ? ""

                                    : first
                                    .getEmployee()
                                    .getLastName();


                    String secondLastName =
                            second
                                    .getEmployee()
                                    .getLastName()
                                    == null

                                    ? ""

                                    : second
                                    .getEmployee()
                                    .getLastName();


                    return firstLastName
                            .compareToIgnoreCase(
                                    secondLastName
                            );
                }
        );


        return rows;
    }


    /*
     * ==========================================
     * تولید Summary کارکنان
     * ==========================================
     */

    private List<AttendanceSummaryReportRow>
    generateSummaryForEmployees(

            List<Employee> employees,

            LocalDate fromDate,

            LocalDate toDate

    ) {


        List<AttendanceSummaryReportRow> rows =
                new ArrayList<>();


        for (Employee employee : employees) {


            rows.addAll(

                    attendanceSummaryReportService
                            .generateReport(

                                    fromDate,

                                    toDate,

                                    employee.getId()
                            )
            );
        }


        return rows;
    }


    /*
     * ==========================================
     * اعتبارسنجی بازه تاریخ
     * ==========================================
     */

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
    }


    /*
     * ==========================================
     * شمارش Status
     * ==========================================
     */

    private long countStatus(

            List<DailyAttendanceReportRow> rows,

            DailyAttendanceStatus status

    ) {


        return rows
                .stream()

                .filter(row ->
                        row.getStatus()
                                ==
                                status
                )

                .count();
    }

}