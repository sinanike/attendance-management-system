package ir.ac.sutech.attendance_system.controller;

import ir.ac.sutech.attendance_system.dto.AttendanceRecordForm;
import ir.ac.sutech.attendance_system.dto.AttendanceSettingsForm;
import ir.ac.sutech.attendance_system.dto.AttendanceSummaryReportRow;
import ir.ac.sutech.attendance_system.dto.DailyAttendanceReportRow;
import ir.ac.sutech.attendance_system.dto.DailyAttendanceStatus;
import ir.ac.sutech.attendance_system.dto.UserAccountForm;
import ir.ac.sutech.attendance_system.model.AttendanceRecord;
import ir.ac.sutech.attendance_system.model.AttendanceType;
import ir.ac.sutech.attendance_system.model.Role;
import ir.ac.sutech.attendance_system.service.AttendanceSettingsService;
import ir.ac.sutech.attendance_system.service.AttendanceSummaryExcelExporter;
import ir.ac.sutech.attendance_system.service.AttendanceSummaryReportService;
import ir.ac.sutech.attendance_system.service.DailyAttendanceReportService;
import ir.ac.sutech.attendance_system.service.OperatorAttendanceService;
import ir.ac.sutech.attendance_system.service.UserAccountService;

import jakarta.validation.Valid;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Locale;

@Controller
@RequestMapping("/operator")
public class OperatorPortalController {

    private final OperatorAttendanceService
            operatorAttendanceService;

    private final DailyAttendanceReportService
            dailyAttendanceReportService;

    private final AttendanceSummaryReportService
            attendanceSummaryReportService;

    private final AttendanceSummaryExcelExporter
            attendanceSummaryExcelExporter;

    private final UserAccountService
            userAccountService;

    private final AttendanceSettingsService
            attendanceSettingsService;

    public OperatorPortalController(
            OperatorAttendanceService operatorAttendanceService,
            DailyAttendanceReportService dailyAttendanceReportService,
            AttendanceSummaryReportService attendanceSummaryReportService,
            AttendanceSummaryExcelExporter attendanceSummaryExcelExporter,
            UserAccountService userAccountService,
            AttendanceSettingsService attendanceSettingsService
    ) {

        this.operatorAttendanceService =
                operatorAttendanceService;

        this.dailyAttendanceReportService =
                dailyAttendanceReportService;

        this.attendanceSummaryReportService =
                attendanceSummaryReportService;

        this.attendanceSummaryExcelExporter =
                attendanceSummaryExcelExporter;

        this.userAccountService =
                userAccountService;

        this.attendanceSettingsService =
                attendanceSettingsService;
    }

    /*
     * ==========================================
     * ROOT
     * ==========================================
     */

    @GetMapping
    public String root() {

        return "redirect:/operator/dashboard";
    }

    /*
     * ==========================================
     * DASHBOARD
     * ==========================================
     */

    @GetMapping("/dashboard")
    public String dashboard(
            Model model
    ) {

        LocalDate today =
                LocalDate.now();

        List<DailyAttendanceReportRow> todayRows;

        try {

            todayRows =
                    dailyAttendanceReportService
                            .generateReport(
                                    today,
                                    today,
                                    null
                            );

        } catch (
                IllegalArgumentException exception
        ) {

            todayRows =
                    List.of();

            model.addAttribute(
                    "errorMessage",
                    exception.getMessage()
            );
        }

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

        long lateCount =
                todayRows
                        .stream()
                        .filter(
                                DailyAttendanceReportRow::isLate
                        )
                        .count();

        List<AttendanceRecord> recentRecords =
                operatorAttendanceService
                        .findAllRecords()
                        .stream()
                        .limit(8)
                        .toList();

        model.addAttribute(
                "today",
                today
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
                "lateCount",
                lateCount
        );

        model.addAttribute(
                "activeEmployeeCount",
                operatorAttendanceService
                        .findActiveEmployees()
                        .size()
        );

        model.addAttribute(
                "accountCount",
                userAccountService
                        .countOperatorManageableAccounts()
        );

        model.addAttribute(
                "recentRecords",
                recentRecords
        );

        model.addAttribute(
                "settings",
                attendanceSettingsService
                        .getSettings()
        );

        return "operator-dashboard";
    }

    /*
     * ==========================================
     * ATTENDANCE RECORDS
     * ==========================================
     */

    @GetMapping("/attendance")
    public String attendance(
            Model model
    ) {

        List<AttendanceRecord> records =
                operatorAttendanceService
                        .findAllRecords();

        model.addAttribute(
                "attendanceRecords",
                records
        );

        model.addAttribute(
                "recordCount",
                records.size()
        );

        return "operator-attendance";
    }

    @GetMapping("/attendance/new")
    public String newAttendance(
            @RequestParam(required = false)
            Long employeeId,
            Model model
    ) {

        AttendanceRecordForm form =
                new AttendanceRecordForm();

        if (employeeId != null) {
            form.setEmployeeId(employeeId);
        }

        prepareAttendanceForm(
                model,
                form,
                null,
                false
        );

        return "operator-attendance-form";
    }

    @PostMapping("/attendance")
    public String createAttendance(
            @Valid
            @ModelAttribute("form")
            AttendanceRecordForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {

        if (bindingResult.hasErrors()) {

            prepareAttendanceForm(
                    model,
                    form,
                    null,
                    false
            );

            return "operator-attendance-form";
        }

        try {

            operatorAttendanceService
                    .createCorrection(
                            form
                    );

            redirectAttributes
                    .addFlashAttribute(
                            "successMessage",
                            "اصلاح دستی تردد با موفقیت ثبت شد."
                    );

            return "redirect:/operator/attendance";

        } catch (
                IllegalArgumentException exception
        ) {

            bindingResult.reject(
                    "attendance.create.failed",
                    exception.getMessage()
            );

            prepareAttendanceForm(
                    model,
                    form,
                    null,
                    false
            );

            return "operator-attendance-form";
        }
    }

    @GetMapping("/attendance/{recordId}/edit")
    public String editAttendance(
            @PathVariable
            Long recordId,
            Model model,
            RedirectAttributes redirectAttributes
    ) {

        try {

            AttendanceRecordForm form =
                    operatorAttendanceService
                            .findFormByRecordId(
                                    recordId
                            );

            prepareAttendanceForm(
                    model,
                    form,
                    recordId,
                    true
            );

            return "operator-attendance-form";

        } catch (
                IllegalArgumentException exception
        ) {

            redirectAttributes
                    .addFlashAttribute(
                            "errorMessage",
                            exception.getMessage()
                    );

            return "redirect:/operator/attendance";
        }
    }

    @PostMapping("/attendance/{recordId}")
    public String updateAttendance(
            @PathVariable
            Long recordId,
            @Valid
            @ModelAttribute("form")
            AttendanceRecordForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {

        if (bindingResult.hasErrors()) {

            prepareAttendanceForm(
                    model,
                    form,
                    recordId,
                    true
            );

            return "operator-attendance-form";
        }

        try {

            operatorAttendanceService
                    .updateCorrection(
                            recordId,
                            form
                    );

            redirectAttributes
                    .addFlashAttribute(
                            "successMessage",
                            "اصلاح دستی تردد با موفقیت ویرایش شد."
                    );

            return "redirect:/operator/attendance";

        } catch (
                IllegalArgumentException exception
        ) {

            bindingResult.reject(
                    "attendance.update.failed",
                    exception.getMessage()
            );

            prepareAttendanceForm(
                    model,
                    form,
                    recordId,
                    true
            );

            return "operator-attendance-form";
        }
    }

    @PostMapping("/attendance/{recordId}/delete")
    public String deleteAttendance(
            @PathVariable
            Long recordId,
            RedirectAttributes redirectAttributes
    ) {

        try {

            operatorAttendanceService
                    .deleteCorrection(
                            recordId
                    );

            redirectAttributes
                    .addFlashAttribute(
                            "successMessage",
                            "اصلاح دستی تردد حذف شد."
                    );

        } catch (
                IllegalArgumentException exception
        ) {

            redirectAttributes
                    .addFlashAttribute(
                            "errorMessage",
                            exception.getMessage()
                    );
        }

        return "redirect:/operator/attendance";
    }

    /*
     * ==========================================
     * DISCREPANCIES
     * ==========================================
     */

    @GetMapping("/discrepancies")
    public String discrepancies(
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
            fromDate = today;
        }

        if (toDate == null) {
            toDate = today;
        }

        List<DailyAttendanceReportRow>
                discrepancyRows =
                List.of();

        try {

            validateDateRange(
                    fromDate,
                    toDate
            );

            discrepancyRows =
                    dailyAttendanceReportService
                            .generateReport(
                                    fromDate,
                                    toDate,
                                    null
                            )
                            .stream()
                            .filter(row ->
                                    row.getStatus()
                                            ==
                                    DailyAttendanceStatus.INCOMPLETE
                            )
                            .toList();

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
                "discrepancyRows",
                discrepancyRows
        );

        model.addAttribute(
                "discrepancyCount",
                discrepancyRows.size()
        );

        return "operator-discrepancies";
    }

    /*
     * ==========================================
     * REPORTS
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

            @RequestParam(required = false)
            Long employeeId,

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

        List<DailyAttendanceReportRow> dailyRows =
                List.of();

        List<AttendanceSummaryReportRow> summaryRows =
                List.of();

        try {

            validateDateRange(
                    fromDate,
                    toDate
            );

            dailyRows =
                    dailyAttendanceReportService
                            .generateReport(
                                    fromDate,
                                    toDate,
                                    employeeId
                            );

            summaryRows =
                    attendanceSummaryReportService
                            .generateReport(
                                    fromDate,
                                    toDate,
                                    employeeId
                            );

        } catch (
                IllegalArgumentException exception
        ) {

            model.addAttribute(
                    "errorMessage",
                    exception.getMessage()
            );
        }

        addReportStatistics(
                model,
                dailyRows
        );

        model.addAttribute(
                "employees",
                operatorAttendanceService
                        .findActiveEmployees()
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
                "dailyRows",
                dailyRows
        );

        model.addAttribute(
                "summaryRows",
                summaryRows
        );

        return "operator-reports";
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
            LocalDate toDate,

            @RequestParam(required = false)
            Long employeeId
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
                                employeeId
                        );

        byte[] file =
                attendanceSummaryExcelExporter
                        .export(
                                rows,
                                fromDate,
                                toDate
                        );

        String fileName =
                "operator-attendance-summary-"
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
     * USER MANAGEMENT
     * ==========================================
     */

    @GetMapping("/users")
    public String users(
            Model model
    ) {

        var accounts =
                userAccountService
                        .findOperatorManageableAccounts();

        model.addAttribute(
                "accounts",
                accounts
        );

        model.addAttribute(
                "accountCount",
                accounts.size()
        );

        return "operator-users-list";
    }

    @GetMapping("/users/new")
    public String newUser(
            Model model
    ) {

        UserAccountForm form =
                new UserAccountForm();

        form.setActive(true);

        prepareUserForm(
                model,
                form,
                null,
                false
        );

        return "operator-user-form";
    }

    @PostMapping("/users")
    public String createUser(
            @Valid
            @ModelAttribute("form")
            UserAccountForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {

        if (bindingResult.hasErrors()) {

            prepareUserForm(
                    model,
                    form,
                    null,
                    false
            );

            return "operator-user-form";
        }

        try {

            userAccountService
                    .createAccountByOperator(
                            form
                    );

            redirectAttributes
                    .addFlashAttribute(
                            "successMessage",
                            "حساب کاربری با موفقیت ساخته شد."
                    );

            return "redirect:/operator/users";

        } catch (
                IllegalArgumentException exception
        ) {

            bindingResult.reject(
                    "user.create.failed",
                    exception.getMessage()
            );

            prepareUserForm(
                    model,
                    form,
                    null,
                    false
            );

            return "operator-user-form";
        }
    }

    @GetMapping("/users/{accountId}/edit")
    public String editUser(
            @PathVariable
            Long accountId,
            Model model,
            RedirectAttributes redirectAttributes
    ) {

        try {

            UserAccountForm form =
                    userAccountService
                            .findOperatorFormById(
                                    accountId
                            );

            prepareUserForm(
                    model,
                    form,
                    accountId,
                    true
            );

            return "operator-user-form";

        } catch (
                IllegalArgumentException exception
        ) {

            redirectAttributes
                    .addFlashAttribute(
                            "errorMessage",
                            exception.getMessage()
                    );

            return "redirect:/operator/users";
        }
    }

    @PostMapping("/users/{accountId}")
    public String updateUser(
            @PathVariable
            Long accountId,
            @Valid
            @ModelAttribute("form")
            UserAccountForm form,
            BindingResult bindingResult,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes
    ) {

        if (bindingResult.hasErrors()) {

            prepareUserForm(
                    model,
                    form,
                    accountId,
                    true
            );

            return "operator-user-form";
        }

        try {

            userAccountService
                    .updateAccountByOperator(
                            accountId,
                            form,
                            authentication.getName()
                    );

            redirectAttributes
                    .addFlashAttribute(
                            "successMessage",
                            "حساب کاربری ویرایش شد."
                    );

            return "redirect:/operator/users";

        } catch (
                IllegalArgumentException exception
        ) {

            bindingResult.reject(
                    "user.update.failed",
                    exception.getMessage()
            );

            prepareUserForm(
                    model,
                    form,
                    accountId,
                    true
            );

            return "operator-user-form";
        }
    }

    @PostMapping("/users/{accountId}/status")
    public String updateUserStatus(
            @PathVariable
            Long accountId,
            @RequestParam
            boolean active,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {

        try {

            userAccountService
                    .updateStatusByOperator(
                            accountId,
                            active,
                            authentication.getName()
                    );

            redirectAttributes
                    .addFlashAttribute(
                            "successMessage",
                            active
                                    ? "حساب کاربری فعال شد."
                                    : "حساب کاربری غیرفعال شد."
                    );

        } catch (
                IllegalArgumentException exception
        ) {

            redirectAttributes
                    .addFlashAttribute(
                            "errorMessage",
                            exception.getMessage()
                    );
        }

        return "redirect:/operator/users";
    }

    /*
     * ==========================================
     * SETTINGS
     * ==========================================
     */

    @GetMapping("/settings")
    public String settings(
            Model model
    ) {

        if (
                !model.containsAttribute(
                        "form"
                )
        ) {

            model.addAttribute(
                    "form",
                    attendanceSettingsService
                            .getForm()
            );
        }

        model.addAttribute(
                "settings",
                attendanceSettingsService
                        .getSettings()
        );

        return "operator-settings";
    }

    @PostMapping("/settings")
    public String updateSettings(
            @Valid
            @ModelAttribute("form")
            AttendanceSettingsForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {

        if (bindingResult.hasErrors()) {

            model.addAttribute(
                    "settings",
                    attendanceSettingsService
                            .getSettings()
            );

            return "operator-settings";
        }

        attendanceSettingsService
                .update(form);

        redirectAttributes
                .addFlashAttribute(
                        "successMessage",
                        "تنظیمات سامانه با موفقیت ذخیره شد."
                );

        return "redirect:/operator/settings";
    }

    /*
     * ==========================================
     * HELPERS
     * ==========================================
     */

    private void prepareAttendanceForm(
            Model model,
            AttendanceRecordForm form,
            Long recordId,
            boolean editing
    ) {

        model.addAttribute(
                "form",
                form
        );

        model.addAttribute(
                "recordId",
                recordId
        );

        model.addAttribute(
                "editing",
                editing
        );

        model.addAttribute(
                "employees",
                operatorAttendanceService
                        .findActiveEmployees()
        );

        model.addAttribute(
                "attendanceTypes",
                AttendanceType.values()
        );

        model.addAttribute(
                "settings",
                attendanceSettingsService
                        .getSettings()
        );
    }

    private void prepareUserForm(
            Model model,
            UserAccountForm form,
            Long accountId,
            boolean editing
    ) {

        model.addAttribute(
                "form",
                form
        );

        model.addAttribute(
                "accountId",
                accountId
        );

        model.addAttribute(
                "editing",
                editing
        );

        model.addAttribute(
                "roles",
                userAccountService
                        .findOperatorAssignableRoles()
        );

        model.addAttribute(
                "employees",
                userAccountService
                        .findActiveEmployees()
        );
    }

    private void addReportStatistics(
            Model model,
            List<DailyAttendanceReportRow> rows
    ) {

        long presentCount =
                countStatus(
                        rows,
                        DailyAttendanceStatus.PRESENT
                );

        long absentCount =
                countStatus(
                        rows,
                        DailyAttendanceStatus.ABSENT
                );

        long incompleteCount =
                countStatus(
                        rows,
                        DailyAttendanceStatus.INCOMPLETE
                );

        long leaveCount =
                countStatus(
                        rows,
                        DailyAttendanceStatus.LEAVE
                );

        long missionCount =
                countStatus(
                        rows,
                        DailyAttendanceStatus.MISSION
                );

        long holidayCount =
                countStatus(
                        rows,
                        DailyAttendanceStatus.HOLIDAY
                );

        long noShiftCount =
                countStatus(
                        rows,
                        DailyAttendanceStatus.NO_SHIFT
                );

        long totalWorkedMinutes =
                rows.stream()
                        .mapToLong(
                                DailyAttendanceReportRow::getWorkedMinutes
                        )
                        .sum();

        long totalLateMinutes =
                rows.stream()
                        .mapToLong(
                                DailyAttendanceReportRow::getLateMinutes
                        )
                        .sum();

        long totalEarlyLeaveMinutes =
                rows.stream()
                        .mapToLong(
                                DailyAttendanceReportRow::getEarlyLeaveMinutes
                        )
                        .sum();

        long denominator =
                presentCount
                        +
                        absentCount
                        +
                        incompleteCount;

        double attendanceRate =
                denominator == 0
                        ? 0
                        : presentCount
                        *
                        100.0
                        /
                        denominator;

        model.addAttribute(
                "rowCount",
                rows.size()
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
                String.format(
                        Locale.ROOT,
                        "%.1f",
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
