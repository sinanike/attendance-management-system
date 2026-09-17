package ir.ac.sutech.attendance_system.controller;

import ir.ac.sutech.attendance_system.dto.AttendanceSummaryReportRow;
import ir.ac.sutech.attendance_system.dto.DailyAttendanceReportRow;
import ir.ac.sutech.attendance_system.dto.DailyAttendanceStatus;
import ir.ac.sutech.attendance_system.repository.EmployeeRepository;
import ir.ac.sutech.attendance_system.service.AttendanceAiAnalysisService;
import ir.ac.sutech.attendance_system.service.AttendanceSummaryExcelExporter;
import ir.ac.sutech.attendance_system.service.AttendanceSummaryReportService;
import ir.ac.sutech.attendance_system.service.DailyAttendanceExcelExporter;
import ir.ac.sutech.attendance_system.service.DailyAttendanceReportService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.List;
import java.util.Locale;

@Controller
@RequestMapping("/admin/reports")
public class DailyAttendanceReportController {

    private static final Logger logger =
            LoggerFactory.getLogger(
                    DailyAttendanceReportController.class
            );

    private final DailyAttendanceReportService reportService;
    private final DailyAttendanceExcelExporter dailyExcelExporter;
    private final AttendanceSummaryReportService summaryReportService;
    private final AttendanceSummaryExcelExporter summaryExcelExporter;
    private final EmployeeRepository employeeRepository;
    private final AttendanceAiAnalysisService attendanceAiAnalysisService;

    public DailyAttendanceReportController(
            DailyAttendanceReportService reportService,
            DailyAttendanceExcelExporter dailyExcelExporter,
            AttendanceSummaryReportService summaryReportService,
            AttendanceSummaryExcelExporter summaryExcelExporter,
            EmployeeRepository employeeRepository,
            AttendanceAiAnalysisService attendanceAiAnalysisService
    ) {
        this.reportService = reportService;
        this.dailyExcelExporter = dailyExcelExporter;
        this.summaryReportService = summaryReportService;
        this.summaryExcelExporter = summaryExcelExporter;
        this.employeeRepository = employeeRepository;
        this.attendanceAiAnalysisService =
                attendanceAiAnalysisService;
    }

    /*
     * ==========================================
     * گزارش روزانه
     * ==========================================
     */

    @GetMapping("/daily-attendance")
    public String showDailyAttendanceReport(

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

            @RequestParam(
                    required = false,
                    defaultValue = "ALL"
            )
            String status,

            Model model
    ) {

        LocalDate today =
                LocalDate.now();

        if (fromDate == null) {
            fromDate = today;
        }

        if (toDate == null) {
            toDate = today;
        }

        /*
         * ------------------------------------------
         * گزارش کامل قبل از فیلتر وضعیت
         * ------------------------------------------
         */

        List<DailyAttendanceReportRow> allReportRows;

        try {

            allReportRows =
                    reportService.generateReport(
                            fromDate,
                            toDate,
                            employeeId
                    );

        } catch (IllegalArgumentException exception) {

            allReportRows =
                    List.of();

            model.addAttribute(
                    "errorMessage",
                    exception.getMessage()
            );
        }


        /*
         * ------------------------------------------
         * تبدیل پارامتر وضعیت به Enum
         * ------------------------------------------
         */

        DailyAttendanceStatus selectedStatus =
                parseStatus(status);


        /*
         * ------------------------------------------
         * اعمال فیلتر وضعیت فقط روی جدول
         * ------------------------------------------
         */

        List<DailyAttendanceReportRow> reportRows =
                filterByStatus(
                        allReportRows,
                        selectedStatus
                );


        /*
         * ==========================================
         * آمار کلی بازه
         *
         * مهم:
         * این آمار از allReportRows گرفته می‌شود.
         * بنابراین با فیلتر جدول تغییر نمی‌کند.
         * ==========================================
         */

        long presentCount =
                countStatus(
                        allReportRows,
                        DailyAttendanceStatus.PRESENT
                );

        long absentCount =
                countStatus(
                        allReportRows,
                        DailyAttendanceStatus.ABSENT
                );

        long incompleteCount =
                countStatus(
                        allReportRows,
                        DailyAttendanceStatus.INCOMPLETE
                );

        long leaveCount =
                countStatus(
                        allReportRows,
                        DailyAttendanceStatus.LEAVE
                );

        long missionCount =
                countStatus(
                        allReportRows,
                        DailyAttendanceStatus.MISSION
                );

        long holidayCount =
                countStatus(
                        allReportRows,
                        DailyAttendanceStatus.HOLIDAY
                );

        long noShiftCount =
                countStatus(
                        allReportRows,
                        DailyAttendanceStatus.NO_SHIFT
                );

        long lateCount =
                allReportRows.stream()
                        .filter(
                                DailyAttendanceReportRow::isLate
                        )
                        .count();

        long earlyLeaveCount =
                allReportRows.stream()
                        .filter(
                                DailyAttendanceReportRow::isEarlyLeave
                        )
                        .count();


        /*
         * ==========================================
         * Model
         * ==========================================
         */

        model.addAttribute(
                "reportRows",
                reportRows
        );

        model.addAttribute(
                "employees",
                employeeRepository
                        .findByActiveTrueOrderByLastNameAscFirstNameAsc()
        );

        /*
         * وضعیت‌های قابل انتخاب
         */

        model.addAttribute(
                "statuses",
                DailyAttendanceStatus.values()
        );

        model.addAttribute(
                "selectedStatus",
                selectedStatus == null
                        ? "ALL"
                        : selectedStatus.name()
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
                "selectedEmployeeId",
                employeeId
        );


        /*
         * تعداد نتایج فیلترشده
         */

        model.addAttribute(
                "rowCount",
                reportRows.size()
        );


        /*
         * تعداد کل قبل از فیلتر وضعیت
         */

        model.addAttribute(
                "totalRowCount",
                allReportRows.size()
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

        return "daily-attendance-report";
    }


    /*
     * ==========================================
     * Excel گزارش روزانه
     * ==========================================
     */

    @GetMapping("/daily-attendance/excel")
    public ResponseEntity<byte[]> downloadDailyAttendanceExcel(

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

            @RequestParam(
                    required = false,
                    defaultValue = "ALL"
            )
            String status

    ) throws IOException {

        LocalDate today =
                LocalDate.now();

        if (fromDate == null) {
            fromDate = today;
        }

        if (toDate == null) {
            toDate = today;
        }


        /*
         * ابتدا گزارش کامل
         */

        List<DailyAttendanceReportRow> reportRows =
                reportService.generateReport(
                        fromDate,
                        toDate,
                        employeeId
                );


        /*
         * سپس همان فیلتر وضعیت صفحه
         */

        DailyAttendanceStatus selectedStatus =
                parseStatus(status);

        reportRows =
                filterByStatus(
                        reportRows,
                        selectedStatus
                );


        byte[] excelFile =
                dailyExcelExporter.export(
                        reportRows,
                        fromDate,
                        toDate
                );


        String statusPart =
                selectedStatus == null
                        ? ""
                        : "-"
                        + selectedStatus
                        .name()
                        .toLowerCase(
                                Locale.ROOT
                        );


        String fileName =
                "daily-attendance-"
                        + fromDate
                        + "-to-"
                        + toDate
                        + statusPart
                        + ".xlsx";


        return ResponseEntity.ok()

                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\""
                                + fileName
                                + "\""
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
     * گزارش خلاصه کارکرد
     * ==========================================
     */

    @GetMapping("/attendance-summary")
    public String showAttendanceSummaryReport(

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

            @RequestParam(
                    defaultValue = "false"
            )
            boolean analyze,

            Model model
    ) {

        YearMonth currentMonth =
                YearMonth.now();


        if (fromDate == null) {

            fromDate =
                    currentMonth.atDay(1);
        }


        if (toDate == null) {

            toDate =
                    currentMonth.atEndOfMonth();
        }


        YearMonth selectedMonth =
                YearMonth.from(
                        fromDate
                );


        YearMonth previousMonth =
                selectedMonth.minusMonths(1);


        YearMonth nextMonth =
                selectedMonth.plusMonths(1);


        List<AttendanceSummaryReportRow> summaryRows;


        try {

            summaryRows =
                    summaryReportService.generateReport(
                            fromDate,
                            toDate,
                            employeeId
                    );

        } catch (IllegalArgumentException exception) {

            summaryRows =
                    List.of();

            model.addAttribute(
                    "reportError",
                    exception.getMessage()
            );
        }


        /*
         * ==========================================
         * مجموع وضعیت‌ها
         * ==========================================
         */

        long totalPresentDays =
                summaryRows.stream()
                        .mapToLong(
                                AttendanceSummaryReportRow
                                        ::getPresentDays
                        )
                        .sum();


        long totalAbsentDays =
                summaryRows.stream()
                        .mapToLong(
                                AttendanceSummaryReportRow
                                        ::getAbsentDays
                        )
                        .sum();


        long totalIncompleteDays =
                summaryRows.stream()
                        .mapToLong(
                                AttendanceSummaryReportRow
                                        ::getIncompleteDays
                        )
                        .sum();


        long totalLeaveDays =
                summaryRows.stream()
                        .mapToLong(
                                AttendanceSummaryReportRow
                                        ::getLeaveDays
                        )
                        .sum();


        long totalMissionDays =
                summaryRows.stream()
                        .mapToLong(
                                AttendanceSummaryReportRow
                                        ::getMissionDays
                        )
                        .sum();


        long totalHolidayDays =
                summaryRows.stream()
                        .mapToLong(
                                AttendanceSummaryReportRow
                                        ::getHolidayDays
                        )
                        .sum();


        long totalNoShiftDays =
                summaryRows.stream()
                        .mapToLong(
                                AttendanceSummaryReportRow
                                        ::getNoShiftDays
                        )
                        .sum();


        /*
         * ==========================================
         * زمان‌ها
         * ==========================================
         */

        long totalWorkedMinutes =
                summaryRows.stream()
                        .mapToLong(
                                AttendanceSummaryReportRow
                                        ::getTotalWorkedMinutes
                        )
                        .sum();


        long totalLateMinutes =
                summaryRows.stream()
                        .mapToLong(
                                AttendanceSummaryReportRow
                                        ::getTotalLateMinutes
                        )
                        .sum();


        long totalEarlyLeaveMinutes =
                summaryRows.stream()
                        .mapToLong(
                                AttendanceSummaryReportRow
                                        ::getTotalEarlyLeaveMinutes
                        )
                        .sum();


        /*
         * ==========================================
         * Model
         * ==========================================
         */

        model.addAttribute(
                "summaryRows",
                summaryRows
        );


        model.addAttribute(
                "employees",
                employeeRepository
                        .findByActiveTrueOrderByLastNameAscFirstNameAsc()
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
                "selectedEmployeeId",
                employeeId
        );


        model.addAttribute(
                "rowCount",
                summaryRows.size()
        );


        /*
         * ماه قبل
         */

        model.addAttribute(
                "previousMonthFromDate",
                previousMonth.atDay(1)
        );


        model.addAttribute(
                "previousMonthToDate",
                previousMonth.atEndOfMonth()
        );


        /*
         * ماه جاری
         */

        model.addAttribute(
                "currentMonthFromDate",
                currentMonth.atDay(1)
        );


        model.addAttribute(
                "currentMonthToDate",
                currentMonth.atEndOfMonth()
        );


        /*
         * ماه بعد
         */

        model.addAttribute(
                "nextMonthFromDate",
                nextMonth.atDay(1)
        );


        model.addAttribute(
                "nextMonthToDate",
                nextMonth.atEndOfMonth()
        );


        /*
         * مجموع وضعیت‌ها
         */

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


        /*
         * مجموع کارکرد
         */

        model.addAttribute(
                "totalWorkedHours",
                totalWorkedMinutes / 60
        );


        model.addAttribute(
                "totalWorkedRemainingMinutes",
                totalWorkedMinutes % 60
        );


        /*
         * مجموع تأخیر
         */

        model.addAttribute(
                "totalLateHours",
                totalLateMinutes / 60
        );


        model.addAttribute(
                "totalLateRemainingMinutes",
                totalLateMinutes % 60
        );


        /*
         * مجموع خروج زودهنگام
         */

        model.addAttribute(
                "totalEarlyLeaveHours",
                totalEarlyLeaveMinutes / 60
        );


        model.addAttribute(
                "totalEarlyLeaveRemainingMinutes",
                totalEarlyLeaveMinutes % 60
        );


        /*
         * ==========================================
         * Gemini
         * ==========================================
         */

        boolean aiAvailable =
                attendanceAiAnalysisService
                        .isAvailable();


        model.addAttribute(
                "aiAvailable",
                aiAvailable
        );


        if (analyze) {

            if (!aiAvailable) {

                model.addAttribute(
                        "aiError",
                        "سرویس تحلیل هوشمند پیکربندی نشده است."
                );

            } else if (
                    summaryRows.isEmpty()
            ) {

                model.addAttribute(
                        "aiError",
                        "داده‌ای برای تحلیل هوشمند وجود ندارد."
                );

            } else {

                try {

                    String aiAnalysis =
                            attendanceAiAnalysisService
                                    .analyze(
                                            summaryRows,
                                            fromDate,
                                            toDate
                                    );


                    model.addAttribute(
                            "aiAnalysis",
                            aiAnalysis
                    );


                } catch (Exception exception) {

                    logger.error(
                            "Gemini attendance summary analysis failed.",
                            exception
                    );


                    model.addAttribute(
                            "aiError",
                            "در دریافت تحلیل هوشمند خطایی رخ داد. لطفاً دوباره تلاش کنید."
                    );
                }
            }
        }


        return "attendance-summary-report";
    }


    /*
     * ==========================================
     * Excel خلاصه کارکرد
     * ==========================================
     */

    @GetMapping("/attendance-summary/excel")
    public ResponseEntity<byte[]> downloadAttendanceSummaryExcel(

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
            Long employeeId

    ) throws IOException {

        YearMonth currentMonth =
                YearMonth.now();


        if (fromDate == null) {

            fromDate =
                    currentMonth.atDay(1);
        }


        if (toDate == null) {

            toDate =
                    currentMonth.atEndOfMonth();
        }


        List<AttendanceSummaryReportRow> summaryRows =
                summaryReportService.generateReport(
                        fromDate,
                        toDate,
                        employeeId
                );


        byte[] excelFile =
                summaryExcelExporter.export(
                        summaryRows,
                        fromDate,
                        toDate
                );


        String fileName =
                "attendance-summary-"
                        + fromDate
                        + "-to-"
                        + toDate
                        + ".xlsx";


        return ResponseEntity.ok()

                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\""
                                + fileName
                                + "\""
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
     * شمارش وضعیت
     * ==========================================
     */

    private long countStatus(
            List<DailyAttendanceReportRow> rows,
            DailyAttendanceStatus status
    ) {

        return rows.stream()
                .filter(
                        row ->
                                row.getStatus()
                                        == status
                )
                .count();
    }


    /*
     * ==========================================
     * تبدیل String به وضعیت
     * ==========================================
     */

    private DailyAttendanceStatus parseStatus(
            String status
    ) {

        if (
                status == null
                        || status.isBlank()
                        || "ALL".equalsIgnoreCase(status)
        ) {

            return null;
        }


        try {

            return DailyAttendanceStatus.valueOf(
                    status
                            .trim()
                            .toUpperCase(
                                    Locale.ROOT
                            )
            );

        } catch (IllegalArgumentException exception) {

            return null;
        }
    }


    /*
     * ==========================================
     * فیلتر وضعیت
     * ==========================================
     */

    private List<DailyAttendanceReportRow> filterByStatus(

            List<DailyAttendanceReportRow> rows,

            DailyAttendanceStatus selectedStatus
    ) {

        if (selectedStatus == null) {

            return rows;
        }


        return rows.stream()
                .filter(
                        row ->
                                row.getStatus()
                                        == selectedStatus
                )
                .toList();
    }
}