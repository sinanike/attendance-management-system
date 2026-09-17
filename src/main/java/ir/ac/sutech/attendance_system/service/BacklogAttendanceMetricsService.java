package ir.ac.sutech.attendance_system.service;

import ir.ac.sutech.attendance_system.dto.AttendanceMetricRow;
import ir.ac.sutech.attendance_system.dto.DailyAttendanceReportRow;
import ir.ac.sutech.attendance_system.model.Employee;
import ir.ac.sutech.attendance_system.model.EmployeeType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class BacklogAttendanceMetricsService {

    private final DailyAttendanceReportService dailyAttendanceReportService;

    public BacklogAttendanceMetricsService(
            DailyAttendanceReportService dailyAttendanceReportService
    ) {
        this.dailyAttendanceReportService = dailyAttendanceReportService;
    }

    public AttendanceMetricRow getEmployeeMetrics(
            Long employeeId,
            LocalDate fromDate,
            LocalDate toDate
    ) {
        return buildRows(fromDate, toDate, null, false)
                .stream()
                .filter(row -> employeeId.equals(
                        row.getEmployee().getId()
                ))
                .findFirst()
                .orElse(null);
    }

    public List<AttendanceMetricRow> getDepartmentRows(
            Long departmentId,
            LocalDate fromDate,
            LocalDate toDate
    ) {
        return buildRows(
                fromDate,
                toDate,
                departmentId,
                false
        );
    }

    public List<AttendanceMetricRow> getOvertimeRows(
            Long departmentId,
            LocalDate fromDate,
            LocalDate toDate
    ) {
        return buildRows(
                fromDate,
                toDate,
                departmentId,
                true
        );
    }

    public List<AttendanceMetricRow> getInsufficientRows(
            Long departmentId,
            LocalDate fromDate,
            LocalDate toDate,
            double minimumAttendancePercent
    ) {
        return buildRows(
                fromDate,
                toDate,
                departmentId,
                false
        ).stream()
                .filter(row ->
                        row.getAttendanceDenominatorDays() > 0
                )
                .filter(row ->
                        row.getAttendanceRate()
                                < minimumAttendancePercent
                )
                .toList();
    }

    public List<AttendanceMetricRow> buildRows(
            LocalDate fromDate,
            LocalDate toDate,
            Long departmentId,
            boolean serviceOnly
    ) {
        validateDates(fromDate, toDate);

        List<DailyAttendanceReportRow> dailyRows =
                dailyAttendanceReportService.generateReport(
                        fromDate,
                        toDate,
                        null
                );

        Map<Long, Accumulator> grouped =
                new LinkedHashMap<>();

        for (DailyAttendanceReportRow row : dailyRows) {
            Employee employee = row.getEmployee();

            if (departmentId != null
                    && !departmentId.equals(
                    employee.getDepartment().getId()
            )) {
                continue;
            }

            if (serviceOnly
                    && employee.getEmployeeType()
                    != EmployeeType.SERVICE_SUPPORT) {
                continue;
            }

            grouped.computeIfAbsent(
                    employee.getId(),
                    ignored -> new Accumulator(employee)
            ).add(row);
        }

        List<AttendanceMetricRow> result =
                new ArrayList<>();

        for (Accumulator value : grouped.values()) {
            result.add(value.toRow());
        }

        return result;
    }

    private void validateDates(
            LocalDate fromDate,
            LocalDate toDate
    ) {
        if (fromDate == null || toDate == null) {
            throw new IllegalArgumentException(
                    "بازه زمانی گزارش کامل نیست."
            );
        }

        if (fromDate.isAfter(toDate)) {
            throw new IllegalArgumentException(
                    "تاریخ شروع نمی‌تواند بعد از تاریخ پایان باشد."
            );
        }
    }

    private static final class Accumulator {

        private final Employee employee;
        private long present;
        private long absent;
        private long incomplete;
        private long worked;
        private long overtime;
        private long late;
        private long earlyLeave;

        private Accumulator(Employee employee) {
            this.employee = employee;
        }

        private void add(DailyAttendanceReportRow row) {
            String statusName = row.getStatus().name();

            if ("PRESENT".equals(statusName)) {
                present++;
            } else if ("ABSENT".equals(statusName)) {
                absent++;
            } else if ("INCOMPLETE".equals(statusName)) {
                incomplete++;
            }

            worked += Math.max(row.getWorkedMinutes(), 0);
            overtime += Math.max(row.getOvertimeMinutes(), 0);
            late += Math.max(row.getLateMinutes(), 0);
            earlyLeave += Math.max(
                    row.getEarlyLeaveMinutes(),
                    0
            );
        }

        private AttendanceMetricRow toRow() {
            long denominator = present + absent + incomplete;

            double rate = denominator == 0
                    ? 0.0
                    : (present * 100.0) / denominator;

            return new AttendanceMetricRow(
                    employee,
                    present,
                    absent,
                    incomplete,
                    worked,
                    overtime,
                    late,
                    earlyLeave,
                    round(rate)
            );
        }

        private static double round(double value) {
            return Math.round(value * 10.0) / 10.0;
        }
    }
}
