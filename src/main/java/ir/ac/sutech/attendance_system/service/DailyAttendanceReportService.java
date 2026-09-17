package ir.ac.sutech.attendance_system.service;

import ir.ac.sutech.attendance_system.dto.DailyAttendanceReportRow;
import ir.ac.sutech.attendance_system.dto.DailyAttendanceStatus;
import ir.ac.sutech.attendance_system.model.AttendanceRecord;
import ir.ac.sutech.attendance_system.model.AttendanceType;
import ir.ac.sutech.attendance_system.model.Employee;
import ir.ac.sutech.attendance_system.model.EmployeeShiftAssignment;
import ir.ac.sutech.attendance_system.model.EmployeeType;
import ir.ac.sutech.attendance_system.model.LeaveRequest;
import ir.ac.sutech.attendance_system.model.LeaveType;
import ir.ac.sutech.attendance_system.model.Shift;

import ir.ac.sutech.attendance_system.repository.AttendanceRecordRepository;
import ir.ac.sutech.attendance_system.repository.EmployeeRepository;
import ir.ac.sutech.attendance_system.repository.EmployeeShiftAssignmentRepository;
import ir.ac.sutech.attendance_system.repository.LeaveRequestRepository;
import ir.ac.sutech.attendance_system.repository.MissionRequestRepository;
import ir.ac.sutech.attendance_system.repository.OfficialHolidayRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class DailyAttendanceReportService {

    private static final LocalTime ADMINISTRATIVE_START_TIME =
            LocalTime.of(8, 0);

    private static final LocalTime ADMINISTRATIVE_END_TIME =
            LocalTime.of(15, 0);

    private static final int ADMINISTRATIVE_LATE_GRACE_MINUTES = 0;

    private static final int
            ADMINISTRATIVE_EARLY_LEAVE_GRACE_MINUTES = 0;


    private final EmployeeRepository employeeRepository;

    private final AttendanceRecordRepository attendanceRecordRepository;

    private final EmployeeShiftAssignmentRepository
            shiftAssignmentRepository;

    private final LeaveRequestRepository leaveRequestRepository;

    private final MissionRequestRepository missionRequestRepository;

    private final OfficialHolidayRepository officialHolidayRepository;


    public DailyAttendanceReportService(
            EmployeeRepository employeeRepository,
            AttendanceRecordRepository attendanceRecordRepository,
            EmployeeShiftAssignmentRepository shiftAssignmentRepository,
            LeaveRequestRepository leaveRequestRepository,
            MissionRequestRepository missionRequestRepository,
            OfficialHolidayRepository officialHolidayRepository
    ) {

        this.employeeRepository =
                employeeRepository;

        this.attendanceRecordRepository =
                attendanceRecordRepository;

        this.shiftAssignmentRepository =
                shiftAssignmentRepository;

        this.leaveRequestRepository =
                leaveRequestRepository;

        this.missionRequestRepository =
                missionRequestRepository;

        this.officialHolidayRepository =
                officialHolidayRepository;
    }


    /*
     * ==========================================
     * تولید گزارش
     * ==========================================
     */

    @Transactional(readOnly = true)
    public List<DailyAttendanceReportRow> generateReport(
            LocalDate fromDate,
            LocalDate toDate,
            Long employeeId
    ) {

        validateDates(
                fromDate,
                toDate
        );


        List<Employee> employees =
                employeeRepository
                        .findByActiveTrueOrderByLastNameAscFirstNameAsc();


        if (employeeId != null) {

            employees =
                    employees.stream()
                            .filter(employee ->
                                    employeeId.equals(
                                            employee.getId()
                                    )
                            )
                            .toList();
        }


        /*
         * برای پوشش شیفت‌های شب
         */
        LocalDateTime queryStart =
                fromDate
                        .atStartOfDay()
                        .minusHours(6);


        LocalDateTime queryEnd =
                toDate
                        .plusDays(2)
                        .atStartOfDay();


        List<AttendanceRecord> records =
                attendanceRecordRepository
                        .findByEventTimeGreaterThanEqualAndEventTimeLessThanOrderByEventTimeDesc(
                                queryStart,
                                queryEnd
                        );


        List<DailyAttendanceReportRow> reportRows =
                new ArrayList<>();


        LocalDate currentDate =
                fromDate;


        while (!currentDate.isAfter(toDate)) {

            for (Employee employee : employees) {

                reportRows.add(
                        createReportRow(
                                employee,
                                currentDate,
                                records
                        )
                );
            }


            currentDate =
                    currentDate.plusDays(1);
        }


        return reportRows;
    }


    /*
     * ==========================================
     * انتخاب نوع کارمند
     * ==========================================
     */

    private DailyAttendanceReportRow createReportRow(
            Employee employee,
            LocalDate reportDate,
            List<AttendanceRecord> records
    ) {

        if (
                employee.getEmployeeType()
                        == EmployeeType.SERVICE_SUPPORT
        ) {

            return createServiceSupportEmployeeRow(
                    employee,
                    reportDate,
                    records
            );
        }


        return createAdministrativeEmployeeRow(
                employee,
                reportDate,
                records
        );
    }


    /*
     * ==========================================
     * کارکنان اداری
     * ==========================================
     */

    private DailyAttendanceReportRow
    createAdministrativeEmployeeRow(
            Employee employee,
            LocalDate reportDate,
            List<AttendanceRecord> records
    ) {

        /*
         * پنجشنبه و جمعه روز کاری نیست.
         */
        if (!isAdministrativeWorkingDay(reportDate)) {

            return createNoShiftRow(
                    employee,
                    reportDate,
                    null
            );
        }


        LocalDateTime scheduledStart =
                reportDate.atTime(
                        ADMINISTRATIVE_START_TIME
                );


        LocalDateTime scheduledEnd =
                reportDate.atTime(
                        ADMINISTRATIVE_END_TIME
                );


        /*
         * تعطیلی رسمی
         */
        if (isOfficialHoliday(reportDate)) {

            return createSpecialStatusRow(
                    employee,
                    reportDate,
                    null,
                    scheduledStart,
                    scheduledEnd,
                    DailyAttendanceStatus.HOLIDAY
            );
        }


        /*
         * مأموریت تأییدشده
         */
        if (
                hasApprovedMission(
                        employee,
                        reportDate
                )
        ) {

            return createSpecialStatusRow(
                    employee,
                    reportDate,
                    null,
                    scheduledStart,
                    scheduledEnd,
                    DailyAttendanceStatus.MISSION
            );
        }


        /*
         * مرخصی
         */
        LeaveRequest approvedLeave =
                findApprovedLeave(
                        employee,
                        reportDate
                );


        /*
         * مرخصی روزانه، استعلاجی یا سایر
         */
        if (
                isFullDayLeave(
                        approvedLeave
                )
        ) {

            return createSpecialStatusRow(
                    employee,
                    reportDate,
                    null,
                    scheduledStart,
                    scheduledEnd,
                    DailyAttendanceStatus.LEAVE
            );
        }


        LeaveRequest hourlyLeave =
                isHourlyLeave(
                        approvedLeave
                )
                        ? approvedLeave
                        : null;


        /*
         * مرخصی ساعتی اگر کل شیفت را پوشش دهد
         */
        if (
                hourlyLeaveCoversWholeShift(
                        hourlyLeave,
                        reportDate,
                        scheduledStart,
                        scheduledEnd
                )
        ) {

            return createSpecialStatusRow(
                    employee,
                    reportDate,
                    null,
                    scheduledStart,
                    scheduledEnd,
                    DailyAttendanceStatus.LEAVE
            );
        }


        LocalDateTime dayStart =
                reportDate.atStartOfDay();


        LocalDateTime nextDayStart =
                reportDate
                        .plusDays(1)
                        .atStartOfDay();


        LocalDateTime firstCheckIn =
                findFirstCheckIn(
                        employee,
                        records,
                        dayStart,
                        nextDayStart
                );


        LocalDateTime lastCheckOut =
                findLastCheckOut(
                        employee,
                        records,
                        dayStart,
                        nextDayStart
                );


        if (
                firstCheckIn != null
                        &&
                        lastCheckOut != null
                        &&
                        lastCheckOut.isBefore(
                                firstCheckIn
                        )
        ) {

            lastCheckOut = null;
        }


        /*
         * اگر مرخصی ابتدای شیفت باشد،
         * زمان مجاز ورود تغییر می‌کند.
         */
        LocalDateTime effectiveStart =
                adjustStartForHourlyLeave(
                        hourlyLeave,
                        reportDate,
                        scheduledStart,
                        scheduledEnd
                );


        /*
         * اگر مرخصی انتهای شیفت باشد،
         * زمان مجاز خروج تغییر می‌کند.
         */
        LocalDateTime effectiveEnd =
                adjustEndForHourlyLeave(
                        hourlyLeave,
                        reportDate,
                        scheduledStart,
                        scheduledEnd
                );


        long lateMinutes =
                calculateLateMinutes(
                        firstCheckIn,
                        effectiveStart,
                        ADMINISTRATIVE_LATE_GRACE_MINUTES
                );


        long earlyLeaveMinutes =
                calculateEarlyLeaveMinutes(
                        lastCheckOut,
                        effectiveEnd,
                        ADMINISTRATIVE_EARLY_LEAVE_GRACE_MINUTES
                );


        /*
         * اصلاح مهم:
         *
         * قبلاً long بود.
         * الان int است چون breakMinutes در DTO
         * از نوع int است.
         */
        int hourlyLeaveMinutes =
                calculateInternalHourlyLeaveMinutes(
                        hourlyLeave,
                        reportDate,
                        scheduledStart,
                        scheduledEnd
                );


        DailyAttendanceStatus status =
                determineStatus(
                        firstCheckIn,
                        lastCheckOut
                );


        return new DailyAttendanceReportRow(
                employee,
                reportDate,
                null,
                scheduledStart,
                scheduledEnd,
                firstCheckIn,
                lastCheckOut,
                lateMinutes,
                earlyLeaveMinutes,
                hourlyLeaveMinutes,
                status
        );
    }


    /*
     * ==========================================
     * کارکنان خدمات و پشتیبانی
     * ==========================================
     */

    private DailyAttendanceReportRow
    createServiceSupportEmployeeRow(
            Employee employee,
            LocalDate reportDate,
            List<AttendanceRecord> records
    ) {

        EmployeeShiftAssignment assignment =
                shiftAssignmentRepository
                        .findEffectiveAssignment(
                                employee.getId(),
                                reportDate
                        )
                        .orElse(null);


        /*
         * شیفتی برای آن تاریخ ثبت نشده
         */
        if (assignment == null) {

            if (isOfficialHoliday(reportDate)) {

                return createSpecialStatusRow(
                        employee,
                        reportDate,
                        null,
                        null,
                        null,
                        DailyAttendanceStatus.HOLIDAY
                );
            }


            return createNoShiftRow(
                    employee,
                    reportDate,
                    null
            );
        }


        Shift shift =
                assignment.getShift();


        if (shift == null) {

            return createNoShiftRow(
                    employee,
                    reportDate,
                    null
            );
        }


        LocalTime startTime =
                shift.getStartTime();


        LocalTime endTime =
                shift.getEndTime();


        if (
                startTime == null
                        ||
                        endTime == null
        ) {

            return createNoShiftRow(
                    employee,
                    reportDate,
                    shift
            );
        }


        LocalDateTime scheduledStart =
                reportDate.atTime(
                        startTime
                );


        LocalDateTime scheduledEnd;


        /*
         * شیفت شب
         *
         * مثال:
         * 21:00 تا 06:00
         */
        if (!endTime.isAfter(startTime)) {

            scheduledEnd =
                    reportDate
                            .plusDays(1)
                            .atTime(
                                    endTime
                            );

        } else {

            scheduledEnd =
                    reportDate.atTime(
                            endTime
                    );
        }


        /*
         * کارکنان خدمات حتی در تعطیل رسمی
         * ممکن است شیفت کاری داشته باشند.
         *
         * بنابراین وجود تعطیلی رسمی به تنهایی
         * شیفت آن‌ها را حذف نمی‌کند.
         */


        /*
         * مأموریت
         */
        if (
                hasApprovedMission(
                        employee,
                        reportDate
                )
        ) {

            return createSpecialStatusRow(
                    employee,
                    reportDate,
                    shift,
                    scheduledStart,
                    scheduledEnd,
                    DailyAttendanceStatus.MISSION
            );
        }


        LeaveRequest approvedLeave =
                findApprovedLeave(
                        employee,
                        reportDate
                );


        /*
         * مرخصی کامل
         */
        if (
                isFullDayLeave(
                        approvedLeave
                )
        ) {

            return createSpecialStatusRow(
                    employee,
                    reportDate,
                    shift,
                    scheduledStart,
                    scheduledEnd,
                    DailyAttendanceStatus.LEAVE
            );
        }


        LeaveRequest hourlyLeave =
                isHourlyLeave(
                        approvedLeave
                )
                        ? approvedLeave
                        : null;


        /*
         * اگر مرخصی ساعتی کل شیفت را پوشش دهد
         */
        if (
                hourlyLeaveCoversWholeShift(
                        hourlyLeave,
                        reportDate,
                        scheduledStart,
                        scheduledEnd
                )
        ) {

            return createSpecialStatusRow(
                    employee,
                    reportDate,
                    shift,
                    scheduledStart,
                    scheduledEnd,
                    DailyAttendanceStatus.LEAVE
            );
        }


        LocalDateTime checkInWindowStart =
                scheduledStart
                        .minusHours(6);


        LocalDateTime checkOutWindowEnd =
                scheduledEnd
                        .plusHours(6);


        LocalDateTime firstCheckIn =
                findFirstCheckIn(
                        employee,
                        records,
                        checkInWindowStart,
                        checkOutWindowEnd
                );


        LocalDateTime lastCheckOut =
                findLastCheckOut(
                        employee,
                        records,
                        scheduledStart,
                        checkOutWindowEnd
                );


        if (
                firstCheckIn != null
                        &&
                        lastCheckOut != null
                        &&
                        lastCheckOut.isBefore(
                                firstCheckIn
                        )
        ) {

            lastCheckOut = null;
        }


        LocalDateTime effectiveStart =
                adjustStartForHourlyLeave(
                        hourlyLeave,
                        reportDate,
                        scheduledStart,
                        scheduledEnd
                );


        LocalDateTime effectiveEnd =
                adjustEndForHourlyLeave(
                        hourlyLeave,
                        reportDate,
                        scheduledStart,
                        scheduledEnd
                );


        long lateMinutes =
                calculateLateMinutes(
                        firstCheckIn,
                        effectiveStart,
                        shift.getLateGraceMinutes()
                );


        long earlyLeaveMinutes =
                calculateEarlyLeaveMinutes(
                        lastCheckOut,
                        effectiveEnd,
                        shift.getEarlyLeaveGraceMinutes()
                );


        /*
         * اصلاح:
         * اینجا هم int است.
         */
        int hourlyLeaveMinutes =
                calculateInternalHourlyLeaveMinutes(
                        hourlyLeave,
                        reportDate,
                        scheduledStart,
                        scheduledEnd
                );


        DailyAttendanceStatus status =
                determineStatus(
                        firstCheckIn,
                        lastCheckOut
                );


        return new DailyAttendanceReportRow(
                employee,
                reportDate,
                shift,
                scheduledStart,
                scheduledEnd,
                firstCheckIn,
                lastCheckOut,
                lateMinutes,
                earlyLeaveMinutes,
                hourlyLeaveMinutes,
                status
        );
    }


    /*
     * ==========================================
     * مرخصی
     * ==========================================
     */

    private LeaveRequest findApprovedLeave(
            Employee employee,
            LocalDate reportDate
    ) {

        return leaveRequestRepository
                .findApprovedLeaveOnDate(
                        employee.getId(),
                        reportDate
                )
                .orElse(null);
    }


    /*
     * هر مرخصی غیرساعتی،
     * مرخصی کامل روزانه محسوب می‌شود.
     */
    private boolean isFullDayLeave(
            LeaveRequest leave
    ) {

        return leave != null
                &&
                leave.getLeaveType()
                        != LeaveType.HOURLY;
    }


    private boolean isHourlyLeave(
            LeaveRequest leave
    ) {

        return leave != null
                &&
                leave.getLeaveType()
                        == LeaveType.HOURLY
                &&
                leave.getStartTime() != null
                &&
                leave.getEndTime() != null;
    }


    /*
     * تعیین LocalDateTime واقعی مرخصی ساعتی.
     *
     * برای شیفت شب نیز قابل استفاده است.
     */
    private LocalDateTime resolveLeaveDateTime(
            LocalTime leaveTime,
            LocalDate reportDate,
            LocalDateTime scheduledStart,
            LocalDateTime scheduledEnd
    ) {

        LocalDate leaveDate =
                reportDate;


        /*
         * اگر شیفت وارد روز بعد شده باشد.
         *
         * مثال:
         * شیفت 21:00 تا 06:00
         *
         * ساعت 02:00 مربوط به روز بعد است.
         */
        if (
                scheduledEnd
                        .toLocalDate()
                        .isAfter(
                                scheduledStart
                                        .toLocalDate()
                        )
                        &&
                        leaveTime.isBefore(
                                scheduledStart
                                        .toLocalTime()
                        )
        ) {

            leaveDate =
                    reportDate.plusDays(1);
        }


        return leaveDate.atTime(
                leaveTime
        );
    }


    /*
     * ==========================================
     * مرخصی ابتدای شیفت
     * ==========================================
     */

    private LocalDateTime adjustStartForHourlyLeave(
            LeaveRequest leave,
            LocalDate reportDate,
            LocalDateTime scheduledStart,
            LocalDateTime scheduledEnd
    ) {

        if (!isHourlyLeave(leave)) {
            return scheduledStart;
        }


        LocalDateTime leaveStart =
                resolveLeaveDateTime(
                        leave.getStartTime(),
                        reportDate,
                        scheduledStart,
                        scheduledEnd
                );


        LocalDateTime leaveEnd =
                resolveLeaveDateTime(
                        leave.getEndTime(),
                        reportDate,
                        scheduledStart,
                        scheduledEnd
                );


        /*
         * اگر مرخصی از ابتدای شیفت شروع شده باشد
         * پایان مرخصی زمان مجاز ورود است.
         */
        if (
                !leaveStart.isAfter(
                        scheduledStart
                )
                        &&
                        leaveEnd.isAfter(
                                scheduledStart
                        )
        ) {

            return leaveEnd;
        }


        return scheduledStart;
    }


    /*
     * ==========================================
     * مرخصی انتهای شیفت
     * ==========================================
     */

    private LocalDateTime adjustEndForHourlyLeave(
            LeaveRequest leave,
            LocalDate reportDate,
            LocalDateTime scheduledStart,
            LocalDateTime scheduledEnd
    ) {

        if (!isHourlyLeave(leave)) {
            return scheduledEnd;
        }


        LocalDateTime leaveStart =
                resolveLeaveDateTime(
                        leave.getStartTime(),
                        reportDate,
                        scheduledStart,
                        scheduledEnd
                );


        LocalDateTime leaveEnd =
                resolveLeaveDateTime(
                        leave.getEndTime(),
                        reportDate,
                        scheduledStart,
                        scheduledEnd
                );


        /*
         * اگر مرخصی تا انتهای شیفت ادامه دارد
         * شروع مرخصی زمان مجاز خروج است.
         */
        if (
                leaveStart.isBefore(
                        scheduledEnd
                )
                        &&
                        !leaveEnd.isBefore(
                                scheduledEnd
                        )
        ) {

            return leaveStart;
        }


        return scheduledEnd;
    }


    /*
     * ==========================================
     * مرخصی ساعتی کل شیفت
     * ==========================================
     */

    private boolean hourlyLeaveCoversWholeShift(
            LeaveRequest leave,
            LocalDate reportDate,
            LocalDateTime scheduledStart,
            LocalDateTime scheduledEnd
    ) {

        if (!isHourlyLeave(leave)) {
            return false;
        }


        LocalDateTime leaveStart =
                resolveLeaveDateTime(
                        leave.getStartTime(),
                        reportDate,
                        scheduledStart,
                        scheduledEnd
                );


        LocalDateTime leaveEnd =
                resolveLeaveDateTime(
                        leave.getEndTime(),
                        reportDate,
                        scheduledStart,
                        scheduledEnd
                );


        return !leaveStart.isAfter(
                scheduledStart
        )
                &&
                !leaveEnd.isBefore(
                        scheduledEnd
                );
    }


    /*
     * ==========================================
     * مرخصی ساعتی وسط شیفت
     * ==========================================
     *
     * اصلاح اصلی خطای Compile:
     *
     * خروجی این متد int است.
     */
    private int calculateInternalHourlyLeaveMinutes(
            LeaveRequest leave,
            LocalDate reportDate,
            LocalDateTime scheduledStart,
            LocalDateTime scheduledEnd
    ) {

        if (!isHourlyLeave(leave)) {
            return 0;
        }


        LocalDateTime leaveStart =
                resolveLeaveDateTime(
                        leave.getStartTime(),
                        reportDate,
                        scheduledStart,
                        scheduledEnd
                );


        LocalDateTime leaveEnd =
                resolveLeaveDateTime(
                        leave.getEndTime(),
                        reportDate,
                        scheduledStart,
                        scheduledEnd
                );


        /*
         * فقط مرخصی کاملاً داخل شیفت
         */
        if (
                !leaveStart.isAfter(
                        scheduledStart
                )
                        ||
                        !leaveEnd.isBefore(
                                scheduledEnd
                        )
        ) {

            return 0;
        }


        long minutes =
                Duration.between(
                        leaveStart,
                        leaveEnd
                ).toMinutes();


        if (minutes <= 0) {
            return 0;
        }


        /*
         * تبدیل امن long به int
         */
        if (minutes > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }


        return (int) minutes;
    }


    /*
     * ==========================================
     * مأموریت
     * ==========================================
     */

    private boolean hasApprovedMission(
            Employee employee,
            LocalDate reportDate
    ) {

        return missionRequestRepository
                .findApprovedMissionOnDate(
                        employee.getId(),
                        reportDate
                )
                .isPresent();
    }


    /*
     * ==========================================
     * تعطیلات رسمی
     * ==========================================
     */

    private boolean isOfficialHoliday(
            LocalDate reportDate
    ) {

        return officialHolidayRepository
                .findByHolidayDateAndActiveTrue(
                        reportDate
                )
                .isPresent();
    }


    /*
     * ==========================================
     * روزهای کاری کارکنان اداری
     * ==========================================
     */

    private boolean isAdministrativeWorkingDay(
            LocalDate date
    ) {

        return switch (
                date.getDayOfWeek()
                ) {

            case SATURDAY,
                 SUNDAY,
                 MONDAY,
                 TUESDAY,
                 WEDNESDAY -> true;

            case THURSDAY,
                 FRIDAY -> false;
        };
    }


    /*
     * ==========================================
     * ساخت سطرهای ویژه
     * ==========================================
     */

    private DailyAttendanceReportRow
    createSpecialStatusRow(
            Employee employee,
            LocalDate reportDate,
            Shift shift,
            LocalDateTime scheduledStart,
            LocalDateTime scheduledEnd,
            DailyAttendanceStatus status
    ) {

        return new DailyAttendanceReportRow(
                employee,
                reportDate,
                shift,
                scheduledStart,
                scheduledEnd,
                null,
                null,
                0,
                0,
                0,
                status
        );
    }


    private DailyAttendanceReportRow
    createNoShiftRow(
            Employee employee,
            LocalDate reportDate,
            Shift shift
    ) {

        return new DailyAttendanceReportRow(
                employee,
                reportDate,
                shift,
                null,
                null,
                null,
                null,
                0,
                0,
                0,
                DailyAttendanceStatus.NO_SHIFT
        );
    }


    /*
     * ==========================================
     * اولین ورود
     * ==========================================
     */

    private LocalDateTime findFirstCheckIn(
            Employee employee,
            List<AttendanceRecord> records,
            LocalDateTime windowStart,
            LocalDateTime windowEnd
    ) {

        return records.stream()

                .filter(record ->
                        belongsToEmployee(
                                record,
                                employee
                        )
                )

                .filter(record ->
                        record.getAttendanceType()
                                == AttendanceType.ENTRY
                )

                .filter(record ->
                        isInsideWindow(
                                record,
                                windowStart,
                                windowEnd
                        )
                )

                .min(
                        Comparator.comparing(
                                AttendanceRecord::getEventTime
                        )
                )

                .map(
                        AttendanceRecord::getEventTime
                )

                .orElse(null);
    }


    /*
     * ==========================================
     * آخرین خروج
     * ==========================================
     */

    private LocalDateTime findLastCheckOut(
            Employee employee,
            List<AttendanceRecord> records,
            LocalDateTime windowStart,
            LocalDateTime windowEnd
    ) {

        return records.stream()

                .filter(record ->
                        belongsToEmployee(
                                record,
                                employee
                        )
                )

                .filter(record ->
                        record.getAttendanceType()
                                == AttendanceType.EXIT
                )

                .filter(record ->
                        isInsideWindow(
                                record,
                                windowStart,
                                windowEnd
                        )
                )

                .max(
                        Comparator.comparing(
                                AttendanceRecord::getEventTime
                        )
                )

                .map(
                        AttendanceRecord::getEventTime
                )

                .orElse(null);
    }


    private boolean isInsideWindow(
            AttendanceRecord record,
            LocalDateTime windowStart,
            LocalDateTime windowEnd
    ) {

        LocalDateTime eventTime =
                record.getEventTime();


        if (eventTime == null) {
            return false;
        }


        return !eventTime.isBefore(
                windowStart
        )
                &&
                eventTime.isBefore(
                        windowEnd
                );
    }


    private boolean belongsToEmployee(
            AttendanceRecord record,
            Employee employee
    ) {

        return record.getEmployee() != null
                &&
                record.getEmployee()
                        .getId() != null
                &&
                employee.getId() != null
                &&
                record.getEmployee()
                        .getId()
                        .equals(
                                employee.getId()
                        );
    }


    /*
     * ==========================================
     * تأخیر
     * ==========================================
     */

    private long calculateLateMinutes(
            LocalDateTime firstCheckIn,
            LocalDateTime scheduledStart,
            int graceMinutes
    ) {

        if (
                firstCheckIn == null
                        ||
                        scheduledStart == null
        ) {

            return 0;
        }


        LocalDateTime allowedArrivalTime =
                scheduledStart.plusMinutes(
                        Math.max(
                                graceMinutes,
                                0
                        )
                );


        if (
                !firstCheckIn.isAfter(
                        allowedArrivalTime
                )
        ) {

            return 0;
        }


        return Duration.between(
                allowedArrivalTime,
                firstCheckIn
        ).toMinutes();
    }


    /*
     * ==========================================
     * خروج زودهنگام
     * ==========================================
     */

    private long calculateEarlyLeaveMinutes(
            LocalDateTime lastCheckOut,
            LocalDateTime scheduledEnd,
            int graceMinutes
    ) {

        if (
                lastCheckOut == null
                        ||
                        scheduledEnd == null
        ) {

            return 0;
        }


        LocalDateTime allowedLeaveTime =
                scheduledEnd.minusMinutes(
                        Math.max(
                                graceMinutes,
                                0
                        )
                );


        if (
                !lastCheckOut.isBefore(
                        allowedLeaveTime
                )
        ) {

            return 0;
        }


        return Duration.between(
                lastCheckOut,
                allowedLeaveTime
        ).toMinutes();
    }


    /*
     * ==========================================
     * تعیین وضعیت نهایی
     * ==========================================
     */

    private DailyAttendanceStatus determineStatus(
            LocalDateTime firstCheckIn,
            LocalDateTime lastCheckOut
    ) {

        if (
                firstCheckIn == null
                        &&
                        lastCheckOut == null
        ) {

            return DailyAttendanceStatus.ABSENT;
        }


        if (
                firstCheckIn == null
                        ||
                        lastCheckOut == null
        ) {

            return DailyAttendanceStatus.INCOMPLETE;
        }


        return DailyAttendanceStatus.PRESENT;
    }


    /*
     * ==========================================
     * اعتبارسنجی تاریخ
     * ==========================================
     */

    private void validateDates(
            LocalDate fromDate,
            LocalDate toDate
    ) {

        if (
                fromDate == null
                        ||
                        toDate == null
        ) {

            throw new IllegalArgumentException(
                    "تاریخ شروع و پایان گزارش الزامی است."
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
}