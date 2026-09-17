package ir.ac.sutech.attendance_system.controller;

import ir.ac.sutech.attendance_system.dto.AttendanceSummaryReportRow;
import ir.ac.sutech.attendance_system.dto.DailyAttendanceReportRow;
import ir.ac.sutech.attendance_system.dto.DailyAttendanceStatus;
import ir.ac.sutech.attendance_system.dto.LeaveRequestForm;
import ir.ac.sutech.attendance_system.dto.MissionRequestForm;

import ir.ac.sutech.attendance_system.model.Employee;
import ir.ac.sutech.attendance_system.model.LeaveRequest;
import ir.ac.sutech.attendance_system.model.MissionRequest;

import ir.ac.sutech.attendance_system.repository.LeaveRequestRepository;
import ir.ac.sutech.attendance_system.repository.MissionRequestRepository;

import ir.ac.sutech.attendance_system.service.AttendanceSummaryReportService;
import ir.ac.sutech.attendance_system.service.CurrentEmployeeService;
import ir.ac.sutech.attendance_system.service.DailyAttendanceReportService;
import ir.ac.sutech.attendance_system.service.LeaveRequestService;
import ir.ac.sutech.attendance_system.service.MissionRequestService;

import org.springframework.format.annotation.DateTimeFormat;

import org.springframework.security.core.Authentication;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.YearMonth;

import java.util.List;


@Controller
@RequestMapping("/employee")
public class EmployeePortalController {


    private final CurrentEmployeeService
            currentEmployeeService;


    private final DailyAttendanceReportService
            dailyAttendanceReportService;


    private final AttendanceSummaryReportService
            attendanceSummaryReportService;


    private final LeaveRequestRepository
            leaveRequestRepository;


    private final MissionRequestRepository
            missionRequestRepository;


    private final LeaveRequestService
            leaveRequestService;


    private final MissionRequestService
            missionRequestService;


    public EmployeePortalController(

            CurrentEmployeeService currentEmployeeService,

            DailyAttendanceReportService dailyAttendanceReportService,

            AttendanceSummaryReportService attendanceSummaryReportService,

            LeaveRequestRepository leaveRequestRepository,

            MissionRequestRepository missionRequestRepository,

            LeaveRequestService leaveRequestService,

            MissionRequestService missionRequestService

    ) {


        this.currentEmployeeService =
                currentEmployeeService;


        this.dailyAttendanceReportService =
                dailyAttendanceReportService;


        this.attendanceSummaryReportService =
                attendanceSummaryReportService;


        this.leaveRequestRepository =
                leaveRequestRepository;


        this.missionRequestRepository =
                missionRequestRepository;


        this.leaveRequestService =
                leaveRequestService;


        this.missionRequestService =
                missionRequestService;
    }


    /*
     * ==========================================
     * ریشه پنل کارمند
     * ==========================================
     */

    @GetMapping
    public String root() {

        return "redirect:/employee/dashboard";
    }


    /*
     * ==========================================
     * داشبورد کارمند
     * ==========================================
     */

    @GetMapping("/dashboard")
    public String dashboard(

            Authentication authentication,

            Model model

    ) {


        Employee employee =
                currentEmployeeService
                        .getCurrentEmployee(
                                authentication
                        );


        LocalDate today =
                LocalDate.now();


        /*
         * ======================================
         * وضعیت امروز
         * ======================================
         */

        DailyAttendanceReportRow todayRow =
                dailyAttendanceReportService
                        .generateReport(

                                today,

                                today,

                                employee.getId()
                        )
                        .stream()
                        .findFirst()
                        .orElse(null);


        /*
         * ======================================
         * خلاصه ماه جاری
         * ======================================
         */

        YearMonth currentMonth =
                YearMonth.from(
                        today
                );


        LocalDate monthStart =
                currentMonth
                        .atDay(1);


        AttendanceSummaryReportRow monthSummary =
                attendanceSummaryReportService
                        .generateReport(

                                monthStart,

                                today,

                                employee.getId()
                        )
                        .stream()
                        .findFirst()
                        .orElse(null);


        /*
         * ======================================
         * درخواست‌های مرخصی
         * ======================================
         */

        List<LeaveRequest> leaveRequests =
                getEmployeeLeaveRequests(
                        employee.getId()
                );


        /*
         * ======================================
         * درخواست‌های مأموریت
         * ======================================
         */

        List<MissionRequest> missionRequests =
                getEmployeeMissionRequests(
                        employee.getId()
                );


        /*
         * ======================================
         * آمار مرخصی
         * ======================================
         */

        long pendingLeaveCount =
                leaveRequests
                        .stream()
                        .filter(
                                LeaveRequest::isPending
                        )
                        .count();


        long approvedLeaveCount =
                leaveRequests
                        .stream()
                        .filter(
                                LeaveRequest::isApproved
                        )
                        .count();


        long rejectedLeaveCount =
                leaveRequests
                        .stream()
                        .filter(
                                LeaveRequest::isRejected
                        )
                        .count();


        /*
         * ======================================
         * آمار مأموریت
         * ======================================
         */

        long pendingMissionCount =
                missionRequests
                        .stream()
                        .filter(
                                MissionRequest::isPending
                        )
                        .count();


        long approvedMissionCount =
                missionRequests
                        .stream()
                        .filter(
                                MissionRequest::isApproved
                        )
                        .count();


        long rejectedMissionCount =
                missionRequests
                        .stream()
                        .filter(
                                MissionRequest::isRejected
                        )
                        .count();


        /*
         * ======================================
         * مجموع درخواست‌های در انتظار
         * ======================================
         */

        long pendingRequestCount =
                pendingLeaveCount
                        +
                        pendingMissionCount;


        /*
         * ======================================
         * پنج مرخصی اخیر
         * ======================================
         */

        List<LeaveRequest> recentLeaveRequests =
                leaveRequests
                        .stream()
                        .limit(5)
                        .toList();


        /*
         * ======================================
         * پنج مأموریت اخیر
         * ======================================
         */

        List<MissionRequest> recentMissionRequests =
                missionRequests
                        .stream()
                        .limit(5)
                        .toList();


        /*
         * ======================================
         * Model
         * ======================================
         */

        model.addAttribute(
                "employee",
                employee
        );


        model.addAttribute(
                "today",
                today
        );


        model.addAttribute(
                "todayRow",
                todayRow
        );


        model.addAttribute(
                "monthSummary",
                monthSummary
        );


        model.addAttribute(
                "pendingLeaveCount",
                pendingLeaveCount
        );


        model.addAttribute(
                "approvedLeaveCount",
                approvedLeaveCount
        );


        model.addAttribute(
                "rejectedLeaveCount",
                rejectedLeaveCount
        );


        model.addAttribute(
                "pendingMissionCount",
                pendingMissionCount
        );


        model.addAttribute(
                "approvedMissionCount",
                approvedMissionCount
        );


        model.addAttribute(
                "rejectedMissionCount",
                rejectedMissionCount
        );


        model.addAttribute(
                "pendingRequestCount",
                pendingRequestCount
        );


        model.addAttribute(
                "recentLeaveRequests",
                recentLeaveRequests
        );


        model.addAttribute(
                "recentMissionRequests",
                recentMissionRequests
        );


        return "employee-dashboard";
    }


    /*
     * ==========================================
     * حضور و غیاب شخصی
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


            Authentication authentication,

            Model model

    ) {


        Employee employee =
                currentEmployeeService
                        .getCurrentEmployee(
                                authentication
                        );


        LocalDate today =
                LocalDate.now();


        /*
         * تاریخ پیش‌فرض شروع:
         * اول ماه جاری
         */

        if (fromDate == null) {


            fromDate =
                    YearMonth
                            .from(today)
                            .atDay(1);
        }


        /*
         * تاریخ پیش‌فرض پایان:
         * امروز
         */

        if (toDate == null) {


            toDate =
                    today;
        }


        List<DailyAttendanceReportRow>
                reportRows;


        try {


            reportRows =
                    dailyAttendanceReportService
                            .generateReport(

                                    fromDate,

                                    toDate,

                                    employee.getId()
                            );


        } catch (
                IllegalArgumentException exception
        ) {


            reportRows =
                    List.of();


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


        /*
         * ======================================
         * مجموع کارکرد
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


        /*
         * ======================================
         * مجموع تأخیر
         * ======================================
         */

        long lateMinutes =
                reportRows
                        .stream()
                        .mapToLong(
                                DailyAttendanceReportRow
                                        ::getLateMinutes
                        )
                        .sum();


        /*
         * ======================================
         * مجموع خروج زودهنگام
         * ======================================
         */

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
                "employee",
                employee
        );


        model.addAttribute(
                "reportRows",
                reportRows
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


        return "employee-attendance";
    }


    /*
     * ==========================================
     * صفحه مرخصی‌های من
     * ==========================================
     */

    @GetMapping("/leaves")
    public String leaves(

            Authentication authentication,

            Model model

    ) {


        Employee employee =
                currentEmployeeService
                        .getCurrentEmployee(
                                authentication
                        );


        List<LeaveRequest> requests =
                getEmployeeLeaveRequests(
                        employee.getId()
                );


        prepareLeavePage(

                model,

                employee,

                requests,

                new LeaveRequestForm()
        );


        return "employee-leaves";
    }


    /*
     * ==========================================
     * ثبت درخواست مرخصی
     * ==========================================
     */

    @PostMapping("/leaves")
    public String createLeave(

            @ModelAttribute("leaveForm")
            LeaveRequestForm leaveForm,

            Authentication authentication,

            RedirectAttributes redirectAttributes

    ) {


        Employee employee =
                currentEmployeeService
                        .getCurrentEmployee(
                                authentication
                        );


        /*
         * شناسه Employee از سمت مرورگر
         * قابل اعتماد نیست.
         *
         * پس شناسه کارمند Login شده
         * در Backend قرار داده می‌شود.
         */

        leaveForm.setEmployeeId(
                employee.getId()
        );


        try {


            leaveRequestService
                    .createRequest(
                            leaveForm
                    );


            redirectAttributes
                    .addFlashAttribute(

                            "successMessage",

                            "درخواست مرخصی با موفقیت ثبت شد و در انتظار بررسی مدیر قرار گرفت."
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


        return "redirect:/employee/leaves";
    }


    /*
     * ==========================================
     * لغو درخواست مرخصی
     *
     * فقط:
     *
     * 1. درخواست متعلق به کارمند Login شده باشد
     * 2. درخواست PENDING باشد
     *
     * نکته امنیتی:
     *
     * هم ID درخواست و هم Employee ID
     * مستقیماً در Query بررسی می‌شوند.
     * ==========================================
     */

    @PostMapping("/leaves/{id}/cancel")
    public String cancelLeave(

            @PathVariable Long id,

            Authentication authentication,

            RedirectAttributes redirectAttributes

    ) {


        Employee employee =
                currentEmployeeService
                        .getCurrentEmployee(
                                authentication
                        );


        try {


            LeaveRequest leaveRequest =
                    leaveRequestRepository
                            .findByIdAndEmployeeId(

                                    id,

                                    employee.getId()
                            )
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "درخواست مرخصی پیدا نشد یا متعلق به شما نیست."
                                    )
                            );


            /*
             * فقط Pending
             */

            if (!leaveRequest.isPending()) {


                throw new IllegalArgumentException(
                        "فقط درخواست در انتظار بررسی قابل لغو است."
                );
            }


            /*
             * لغو درخواست
             */

            leaveRequestRepository
                    .delete(
                            leaveRequest
                    );


            redirectAttributes
                    .addFlashAttribute(

                            "successMessage",

                            "درخواست مرخصی با موفقیت لغو شد."
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


        return "redirect:/employee/leaves";
    }


    /*
     * ==========================================
     * صفحه مأموریت‌های من
     * ==========================================
     */

    @GetMapping("/missions")
    public String missions(

            Authentication authentication,

            Model model

    ) {


        Employee employee =
                currentEmployeeService
                        .getCurrentEmployee(
                                authentication
                        );


        List<MissionRequest> requests =
                getEmployeeMissionRequests(
                        employee.getId()
                );


        prepareMissionPage(

                model,

                employee,

                requests,

                new MissionRequestForm()
        );


        return "employee-missions";
    }


    /*
     * ==========================================
     * ثبت درخواست مأموریت
     * ==========================================
     */

    @PostMapping("/missions")
    public String createMission(

            @ModelAttribute("missionForm")
            MissionRequestForm missionForm,

            Authentication authentication,

            RedirectAttributes redirectAttributes

    ) {


        Employee employee =
                currentEmployeeService
                        .getCurrentEmployee(
                                authentication
                        );


        /*
         * employeeId فقط از Session تعیین می‌شود.
         */

        missionForm.setEmployeeId(
                employee.getId()
        );


        try {


            missionRequestService
                    .createRequest(
                            missionForm
                    );


            redirectAttributes
                    .addFlashAttribute(

                            "successMessage",

                            "درخواست مأموریت با موفقیت ثبت شد و در انتظار بررسی مدیر قرار گرفت."
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


        return "redirect:/employee/missions";
    }


    /*
     * ==========================================
     * لغو درخواست مأموریت
     *
     * فقط:
     *
     * 1. درخواست متعلق به Employee Login شده
     * 2. درخواست PENDING
     *
     * ID + Employee ID
     * در Query بررسی می‌شوند.
     * ==========================================
     */

    @PostMapping("/missions/{id}/cancel")
    public String cancelMission(

            @PathVariable Long id,

            Authentication authentication,

            RedirectAttributes redirectAttributes

    ) {


        Employee employee =
                currentEmployeeService
                        .getCurrentEmployee(
                                authentication
                        );


        try {


            MissionRequest missionRequest =
                    missionRequestRepository
                            .findByIdAndEmployeeId(

                                    id,

                                    employee.getId()
                            )
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "درخواست مأموریت پیدا نشد یا متعلق به شما نیست."
                                    )
                            );


            /*
             * فقط Pending
             */

            if (!missionRequest.isPending()) {


                throw new IllegalArgumentException(
                        "فقط درخواست در انتظار بررسی قابل لغو است."
                );
            }


            /*
             * لغو درخواست
             */

            missionRequestRepository
                    .delete(
                            missionRequest
                    );


            redirectAttributes
                    .addFlashAttribute(

                            "successMessage",

                            "درخواست مأموریت با موفقیت لغو شد."
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


        return "redirect:/employee/missions";
    }


    /*
     * ==========================================
     * دریافت مرخصی‌های کارمند
     *
     * دیگر از findAll() استفاده نمی‌کنیم.
     * PostgreSQL فقط درخواست‌های همین Employee
     * را برمی‌گرداند.
     * ==========================================
     */

    private List<LeaveRequest>
    getEmployeeLeaveRequests(
            Long employeeId
    ) {


        return leaveRequestRepository
                .findByEmployeeIdOrderByCreatedAtDescIdDesc(
                        employeeId
                );
    }


    /*
     * ==========================================
     * دریافت مأموریت‌های کارمند
     * ==========================================
     */

    private List<MissionRequest>
    getEmployeeMissionRequests(
            Long employeeId
    ) {


        return missionRequestRepository
                .findByEmployeeIdOrderByCreatedAtDescIdDesc(
                        employeeId
                );
    }


    /*
     * ==========================================
     * آماده‌سازی صفحه مرخصی
     * ==========================================
     */

    private void prepareLeavePage(

            Model model,

            Employee employee,

            List<LeaveRequest> requests,

            LeaveRequestForm form

    ) {


        model.addAttribute(
                "employee",
                employee
        );


        model.addAttribute(
                "leaveRequests",
                requests
        );


        model.addAttribute(
                "leaveForm",
                form
        );


        model.addAttribute(
                "totalCount",
                requests.size()
        );


        model.addAttribute(

                "pendingCount",

                requests
                        .stream()
                        .filter(
                                LeaveRequest::isPending
                        )
                        .count()
        );


        model.addAttribute(

                "approvedCount",

                requests
                        .stream()
                        .filter(
                                LeaveRequest::isApproved
                        )
                        .count()
        );


        model.addAttribute(

                "rejectedCount",

                requests
                        .stream()
                        .filter(
                                LeaveRequest::isRejected
                        )
                        .count()
        );
    }


    /*
     * ==========================================
     * آماده‌سازی صفحه مأموریت
     * ==========================================
     */

    private void prepareMissionPage(

            Model model,

            Employee employee,

            List<MissionRequest> requests,

            MissionRequestForm form

    ) {


        model.addAttribute(
                "employee",
                employee
        );


        model.addAttribute(
                "missionRequests",
                requests
        );


        model.addAttribute(
                "missionForm",
                form
        );


        model.addAttribute(
                "totalCount",
                requests.size()
        );


        model.addAttribute(

                "pendingCount",

                requests
                        .stream()
                        .filter(
                                MissionRequest::isPending
                        )
                        .count()
        );


        model.addAttribute(

                "approvedCount",

                requests
                        .stream()
                        .filter(
                                MissionRequest::isApproved
                        )
                        .count()
        );


        model.addAttribute(

                "rejectedCount",

                requests
                        .stream()
                        .filter(
                                MissionRequest::isRejected
                        )
                        .count()
        );
    }


    /*
     * ==========================================
     * شمارش وضعیت حضور
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