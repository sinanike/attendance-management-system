package ir.ac.sutech.attendance_system.service;

import ir.ac.sutech.attendance_system.dto.DailyAttendanceReportRow;
import ir.ac.sutech.attendance_system.dto.LeaveBalanceView;
import ir.ac.sutech.attendance_system.model.Employee;
import ir.ac.sutech.attendance_system.util.JalaliDateUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@Transactional(readOnly = true)
public class LeaveBalanceService {

    private final DailyAttendanceReportService dailyAttendanceReportService;
    private final AttendanceRuleSettingsService attendanceRuleSettingsService;
    private final JalaliDateUtil jalaliDateUtil;

    public LeaveBalanceService(
            DailyAttendanceReportService dailyAttendanceReportService,
            AttendanceRuleSettingsService attendanceRuleSettingsService,
            JalaliDateUtil jalaliDateUtil
    ) {
        this.dailyAttendanceReportService = dailyAttendanceReportService;
        this.attendanceRuleSettingsService = attendanceRuleSettingsService;
        this.jalaliDateUtil = jalaliDateUtil;
    }

    public LeaveBalanceView getBalance(
            Employee employee,
            int year
    ) {
        if (year < 1300 || year > 1600) {
            throw new IllegalArgumentException(
                    "سال انتخاب‌شده معتبر نیست."
            );
        }

        LocalDate fromDate = jalaliDateUtil.startOfJalaliYear(year);
        LocalDate toDate = jalaliDateUtil.endOfJalaliYear(year);

        long usedDays = dailyAttendanceReportService
                .generateReport(
                        fromDate,
                        toDate,
                        employee.getId()
                )
                .stream()
                .map(DailyAttendanceReportRow::getStatus)
                .filter(status -> "LEAVE".equals(status.name()))
                .count();

        int annualAllowance =
                attendanceRuleSettingsService
                        .getSettings()
                        .getAnnualLeaveDays();

        return new LeaveBalanceView(
                year,
                annualAllowance,
                usedDays
        );
    }
}
