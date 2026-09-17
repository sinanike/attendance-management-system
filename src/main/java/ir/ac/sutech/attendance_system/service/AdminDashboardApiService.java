package ir.ac.sutech.attendance_system.service;

import ir.ac.sutech.attendance_system.dto.AdminDashboardAiResponse;
import ir.ac.sutech.attendance_system.dto.AdminDashboardDataResponse;
import ir.ac.sutech.attendance_system.dto.AdminDashboardOverviewResponse;
import ir.ac.sutech.attendance_system.dto.AttendanceSummaryReportRow;
import ir.ac.sutech.attendance_system.dto.DailyAttendanceReportRow;
import ir.ac.sutech.attendance_system.dto.DailyAttendanceStatus;

import ir.ac.sutech.attendance_system.model.AttendanceRecord;
import ir.ac.sutech.attendance_system.model.Department;
import ir.ac.sutech.attendance_system.model.Employee;
import ir.ac.sutech.attendance_system.model.LeaveRequest;
import ir.ac.sutech.attendance_system.model.MissionRequest;

import ir.ac.sutech.attendance_system.repository.AttendanceRecordRepository;
import ir.ac.sutech.attendance_system.repository.DepartmentRepository;
import ir.ac.sutech.attendance_system.repository.EmployeeRepository;
import ir.ac.sutech.attendance_system.repository.LeaveRequestRepository;
import ir.ac.sutech.attendance_system.repository.MissionRequestRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class AdminDashboardApiService {

    private static final int TREND_DAYS = 30;

    private static final int RECENT_ATTENDANCE_LIMIT = 5;

    private static final int PENDING_REQUEST_LIMIT = 5;


    private final DailyAttendanceReportService
            dailyAttendanceReportService;

    private final AttendanceSummaryReportService
            attendanceSummaryReportService;

    private final AttendanceAiAnalysisService
            attendanceAiAnalysisService;

    private final EmployeeRepository
            employeeRepository;

    private final DepartmentRepository
            departmentRepository;

    private final AttendanceRecordRepository
            attendanceRecordRepository;

    private final LeaveRequestRepository
            leaveRequestRepository;

    private final MissionRequestRepository
            missionRequestRepository;


    public AdminDashboardApiService(

            DailyAttendanceReportService dailyAttendanceReportService,

            AttendanceSummaryReportService attendanceSummaryReportService,

            AttendanceAiAnalysisService attendanceAiAnalysisService,

            EmployeeRepository employeeRepository,

            DepartmentRepository departmentRepository,

            AttendanceRecordRepository attendanceRecordRepository,

            LeaveRequestRepository leaveRequestRepository,

            MissionRequestRepository missionRequestRepository

    ) {

        this.dailyAttendanceReportService =
                dailyAttendanceReportService;

        this.attendanceSummaryReportService =
                attendanceSummaryReportService;

        this.attendanceAiAnalysisService =
                attendanceAiAnalysisService;

        this.employeeRepository =
                employeeRepository;

        this.departmentRepository =
                departmentRepository;

        this.attendanceRecordRepository =
                attendanceRecordRepository;

        this.leaveRequestRepository =
                leaveRequestRepository;

        this.missionRequestRepository =
                missionRequestRepository;
    }


    /*
     * ==========================================
     * کارت‌های بالای داشبورد
     * ==========================================
     */

    public AdminDashboardOverviewResponse
    getOverview() {

        LocalDate today =
                LocalDate.now();


        long activeEmployeeCount =
                employeeRepository
                        .findByActiveTrueOrderByLastNameAscFirstNameAsc()
                        .size();


        List<DailyAttendanceReportRow>
                todayRows =
                dailyAttendanceReportService
                        .generateReport(
                                today,
                                today,
                                null
                        );


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


        long denominator =
                presentCount
                        +
                        absentCount
                        +
                        incompleteCount;


        double attendanceRate =
                denominator == 0

                        ? 0.0

                        : presentCount
                        * 100.0
                        / denominator;


        attendanceRate =
                roundOneDecimal(
                        attendanceRate
                );


        return new AdminDashboardOverviewResponse(

                activeEmployeeCount,

                presentCount,

                absentCount,

                leaveCount,

                missionCount,

                lateCount,

                incompleteCount,

                attendanceRate,

                today,

                LocalDateTime.now()
        );
    }


    /*
     * ==========================================
     * کل داده‌های داشبورد
     *
     * این Endpoint توسط React هر 10 ثانیه
     * خوانده می‌شود.
     * ==========================================
     */

    public AdminDashboardDataResponse
    getDashboardData() {

        LocalDate today =
                LocalDate.now();


        LocalDate fromDate =
                today.minusDays(
                        TREND_DAYS - 1L
                );


        AdminDashboardOverviewResponse overview =
                getOverview();


        /*
         * یک بار گزارش 30 روزه ساخته می‌شود
         * و هم Trend و هم Department Chart
         * از همان داده استفاده می‌کنند.
         */

        List<DailyAttendanceReportRow>
                thirtyDayRows =
                dailyAttendanceReportService
                        .generateReport(
                                fromDate,
                                today,
                                null
                        );


        List<AdminDashboardDataResponse.TrendPoint>
                trend =
                buildTrend(
                        thirtyDayRows,
                        fromDate,
                        today
                );


        List<AdminDashboardDataResponse.StatusPoint>
                status =
                buildTodayStatus(
                        overview
                );


        List<AdminDashboardDataResponse.DepartmentPoint>
                departments =
                buildDepartmentPerformance(
                        thirtyDayRows
                );


        List<AdminDashboardDataResponse.RecentAttendanceItem>
                recentAttendance =
                buildRecentAttendance();


        List<AdminDashboardDataResponse.PendingRequestItem>
                pendingRequests =
                buildPendingRequests();


        List<AdminDashboardDataResponse.AlertItem>
                alerts =
                buildAlerts(
                        overview,
                        pendingRequests.size()
                );


        return new AdminDashboardDataResponse(

                overview,

                trend,

                status,

                departments,

                recentAttendance,

                pendingRequests,

                alerts,

                LocalDateTime.now()
        );
    }


    /*
     * ==========================================
     * تحلیل واقعی Gemini
     *
     * جدا از Refresh ده‌ثانیه‌ای اجرا می‌شود
     * تا API هوش مصنوعی بی‌دلیل مصرف نشود.
     * ==========================================
     */

    public AdminDashboardAiResponse
    getAiAnalysis() {

        if (
                !attendanceAiAnalysisService
                        .isAvailable()
        ) {

            return new AdminDashboardAiResponse(

                    false,

                    "سرویس تحلیل هوشمند در حال حاضر پیکربندی نشده است.",

                    LocalDateTime.now()
            );
        }


        LocalDate today =
                LocalDate.now();


        LocalDate fromDate =
                today.minusDays(
                        TREND_DAYS - 1L
                );


        try {

            List<AttendanceSummaryReportRow>
                    summaryRows =
                    attendanceSummaryReportService
                            .generateReport(
                                    fromDate,
                                    today,
                                    null
                            );


            String analysis =
                    attendanceAiAnalysisService
                            .analyze(
                                    summaryRows,
                                    fromDate,
                                    today
                            );


            return new AdminDashboardAiResponse(

                    true,

                    analysis,

                    LocalDateTime.now()
            );


        } catch (Exception exception) {

            return new AdminDashboardAiResponse(

                    true,

                    "در دریافت تحلیل هوشمند خطایی رخ داد. لطفاً چند دقیقه بعد دوباره تلاش کنید.",

                    LocalDateTime.now()
            );
        }
    }


    /*
     * ==========================================
     * Trend 30 روز اخیر
     * ==========================================
     */

    private List<AdminDashboardDataResponse.TrendPoint>
    buildTrend(

            List<DailyAttendanceReportRow> rows,

            LocalDate fromDate,

            LocalDate toDate

    ) {

        List<AdminDashboardDataResponse.TrendPoint>
                result =
                new ArrayList<>();


        LocalDate current =
                fromDate;


        while (
                !current.isAfter(
                        toDate
                )
        ) {

            LocalDate date =
                    current;


            List<DailyAttendanceReportRow>
                    dayRows =
                    rows
                            .stream()
                            .filter(row ->
                                    date.equals(
                                            row.getReportDate()
                                    )
                            )
                            .toList();


            long present =
                    countStatus(
                            dayRows,
                            DailyAttendanceStatus.PRESENT
                    );


            long absent =
                    countStatus(
                            dayRows,
                            DailyAttendanceStatus.ABSENT
                    );


            long incomplete =
                    countStatus(
                            dayRows,
                            DailyAttendanceStatus.INCOMPLETE
                    );


            long denominator =
                    present
                            +
                            absent
                            +
                            incomplete;


            /*
             * روزهایی که هیچ Employee
             * مشمول ارزیابی حضور نیست
             * از Trend حذف می‌شوند.
             */

            if (denominator > 0) {

                double rate =
                        present
                        * 100.0
                        / denominator;


                result.add(

                        new AdminDashboardDataResponse.TrendPoint(

                                date,

                                roundOneDecimal(
                                        rate
                                )
                        )
                );
            }


            current =
                    current.plusDays(1);
        }


        return result;
    }


    /*
     * ==========================================
     * Donut وضعیت امروز
     * ==========================================
     */

    private List<AdminDashboardDataResponse.StatusPoint>
    buildTodayStatus(

            AdminDashboardOverviewResponse overview

    ) {

        return List.of(

                new AdminDashboardDataResponse.StatusPoint(
                        "حاضر",
                        overview.presentCount()
                ),

                new AdminDashboardDataResponse.StatusPoint(
                        "غایب",
                        overview.absentCount()
                ),

                new AdminDashboardDataResponse.StatusPoint(
                        "مرخصی",
                        overview.leaveCount()
                ),

                new AdminDashboardDataResponse.StatusPoint(
                        "مأموریت",
                        overview.missionCount()
                ),

                new AdminDashboardDataResponse.StatusPoint(
                        "ورود ناقص",
                        overview.incompleteCount()
                )
        );
    }


    /*
     * ==========================================
     * مقایسه واحدها در 30 روز اخیر
     * ==========================================
     */

    private List<AdminDashboardDataResponse.DepartmentPoint>
    buildDepartmentPerformance(

            List<DailyAttendanceReportRow> rows

    ) {

        List<Department> activeDepartments =
                departmentRepository
                        .findAllByActiveTrueOrderByNameAsc();


        List<AdminDashboardDataResponse.DepartmentPoint>
                result =
                new ArrayList<>();


        for (
                Department department
                :
                activeDepartments
        ) {

            List<DailyAttendanceReportRow>
                    departmentRows =
                    rows
                            .stream()
                            .filter(row ->
                                    row.getEmployee()
                                            != null
                                            &&
                                            row.getEmployee()
                                                    .getDepartment()
                                            != null
                                            &&
                                            department
                                                    .getId()
                                                    .equals(
                                                            row
                                                                    .getEmployee()
                                                                    .getDepartment()
                                                                    .getId()
                                                    )
                            )
                            .toList();


            long present =
                    countStatus(
                            departmentRows,
                            DailyAttendanceStatus.PRESENT
                    );


            long absent =
                    countStatus(
                            departmentRows,
                            DailyAttendanceStatus.ABSENT
                    );


            long incomplete =
                    countStatus(
                            departmentRows,
                            DailyAttendanceStatus.INCOMPLETE
                    );


            long denominator =
                    present
                            +
                            absent
                            +
                            incomplete;


            double rate =
                    denominator == 0

                            ? 0

                            : present
                            * 100.0
                            / denominator;


            result.add(

                    new AdminDashboardDataResponse.DepartmentPoint(

                            department.getId(),

                            department.getName(),

                            roundOneDecimal(
                                    rate
                            )
                    )
            );
        }


        return result;
    }


    /*
     * ==========================================
     * پنج تردد واقعی اخیر
     * ==========================================
     */

    private List<AdminDashboardDataResponse.RecentAttendanceItem>
    buildRecentAttendance() {

        DateTimeFormatter timeFormatter =
                DateTimeFormatter.ofPattern(
                        "HH:mm",
                        Locale.ROOT
                );


        return attendanceRecordRepository
                .findAllByOrderByEventTimeDesc()
                .stream()
                .filter(record ->
                        record.getEmployee()
                                != null
                                &&
                                record.getEventTime()
                                        != null
                )
                .limit(
                        RECENT_ATTENDANCE_LIMIT
                )
                .map(record -> {

                    Employee employee =
                            record.getEmployee();


                    String attendanceTypeName =
                            record.getAttendanceType()
                                    == null

                                    ? ""

                                    : record
                                            .getAttendanceType()
                                            .name();


                    boolean checkIn =
                            attendanceTypeName
                                    .contains(
                                            "CHECK_IN"
                                    )
                                    ||
                                    attendanceTypeName
                                            .contains(
                                                    "ENTRY"
                                            );


                    String type =
                            checkIn
                                    ? "ورود"
                                    : "خروج";


                    String state =
                            checkIn
                                    ? "ok"
                                    : "out";


                    String departmentName =
                            employee.getDepartment()
                                    == null

                                    ? "—"

                                    : employee
                                            .getDepartment()
                                            .getName();


                    return new AdminDashboardDataResponse.RecentAttendanceItem(

                            record.getId(),

                            fullName(
                                    employee
                            ),

                            departmentName,

                            type,

                            record
                                    .getEventTime()
                                    .toLocalDate(),

                            record
                                    .getEventTime()
                                    .format(
                                            timeFormatter
                                    ),

                            state
                    );
                })
                .toList();
    }


    /*
     * ==========================================
     * درخواست‌های Pending واقعی
     * ==========================================
     */

    private List<AdminDashboardDataResponse.PendingRequestItem>
    buildPendingRequests() {

        List<AdminDashboardDataResponse.PendingRequestItem>
                items =
                new ArrayList<>();


        for (
                LeaveRequest request
                :
                leaveRequestRepository
                        .findAll()
        ) {

            if (
                    !request.isPending()
                            ||
                            request.getEmployee()
                            == null
            ) {

                continue;
            }


            Employee employee =
                    request.getEmployee();


            items.add(

                    new AdminDashboardDataResponse.PendingRequestItem(

                            "LEAVE-"
                                    +
                                    request.getId(),

                            leaveTitle(
                                    request
                                            .getLeaveType()
                                            == null

                                            ? ""

                                            : request
                                                    .getLeaveType()
                                                    .name()
                            ),

                            fullName(
                                    employee
                            ),

                            departmentName(
                                    employee
                            ),

                            request
                                    .getStartDate(),

                            "در انتظار",

                            request
                                    .getCreatedAt()
                    )
            );
        }


        for (
                MissionRequest request
                :
                missionRequestRepository
                        .findAll()
        ) {

            if (
                    !request.isPending()
                            ||
                            request.getEmployee()
                            == null
            ) {

                continue;
            }


            Employee employee =
                    request.getEmployee();


            items.add(

                    new AdminDashboardDataResponse.PendingRequestItem(

                            "MISSION-"
                                    +
                                    request.getId(),

                            "مأموریت",

                            fullName(
                                    employee
                            ),

                            departmentName(
                                    employee
                            ),

                            request
                                    .getStartDate(),

                            "در انتظار",

                            request
                                    .getCreatedAt()
                    )
            );
        }


        items.sort(

                Comparator
                        .comparing(
                                AdminDashboardDataResponse
                                        .PendingRequestItem
                                        ::createdAt,

                                Comparator
                                        .nullsLast(
                                                Comparator
                                                        .naturalOrder()
                                        )
                        )
                        .reversed()
        );


        return items
                .stream()
                .limit(
                        PENDING_REQUEST_LIMIT
                )
                .toList();
    }


    /*
     * ==========================================
     * هشدارهای واقعی
     * ==========================================
     */

    private List<AdminDashboardDataResponse.AlertItem>
    buildAlerts(

            AdminDashboardOverviewResponse overview,

            int visiblePendingRequestCount

    ) {

        List<AdminDashboardDataResponse.AlertItem>
                alerts =
                new ArrayList<>();


        if (
                overview.absentCount()
                        > 0
        ) {

            alerts.add(

                    new AdminDashboardDataResponse.AlertItem(

                            "absence",

                            "red",

                            overview.absentCount()
                                    +
                                    " کارمند امروز غایب هستند."
                    )
            );
        }


        if (
                overview.lateCount()
                        > 0
        ) {

            alerts.add(

                    new AdminDashboardDataResponse.AlertItem(

                            "late",

                            "orange",

                            overview.lateCount()
                                    +
                                    " مورد تأخیر امروز ثبت شده است."
                    )
            );
        }


        if (
                overview.incompleteCount()
                        > 0
        ) {

            alerts.add(

                    new AdminDashboardDataResponse.AlertItem(

                            "incomplete",

                            "orange",

                            overview.incompleteCount()
                                    +
                                    " تردد ناقص نیازمند بررسی است."
                    )
            );
        }


        if (
                visiblePendingRequestCount
                        > 0
        ) {

            alerts.add(

                    new AdminDashboardDataResponse.AlertItem(

                            "pending",

                            "blue",

                            "درخواست‌های در انتظار برای بررسی وجود دارد."
                    )
            );
        }


        if (
                alerts.isEmpty()
        ) {

            alerts.add(

                    new AdminDashboardDataResponse.AlertItem(

                            "clear",

                            "green",

                            "در حال حاضر هشدار مهمی برای بررسی وجود ندارد."
                    )
            );
        }


        return alerts;
    }


    private String leaveTitle(
            String leaveTypeName
    ) {

        return switch (
                leaveTypeName
        ) {

            case "DAILY" ->
                    "مرخصی روزانه";

            case "HOURLY" ->
                    "مرخصی ساعتی";

            case "SICK" ->
                    "مرخصی استعلاجی";

            case "OTHER" ->
                    "سایر مرخصی‌ها";

            default ->
                    "مرخصی";
        };
    }


    private String fullName(
            Employee employee
    ) {

        String firstName =
                employee.getFirstName()
                        == null

                        ? ""

                        : employee
                                .getFirstName()
                                .trim();


        String lastName =
                employee.getLastName()
                        == null

                        ? ""

                        : employee
                                .getLastName()
                                .trim();


        return (
                firstName
                        +
                        " "
                        +
                        lastName
        )
                .trim();
    }


    private String departmentName(
            Employee employee
    ) {

        return employee.getDepartment()
                == null

                ? "—"

                : employee
                        .getDepartment()
                        .getName();
    }


    private long countStatus(

            List<DailyAttendanceReportRow> rows,

            DailyAttendanceStatus status

    ) {

        return rows
                .stream()
                .filter(row ->
                        row.getStatus()
                                == status
                )
                .count();
    }


    private double roundOneDecimal(
            double value
    ) {

        return Math.round(
                value
                        *
                        10.0
        )
                /
                10.0;
    }
}
