package ir.ac.sutech.attendance_system.service;

import ir.ac.sutech.attendance_system.dto.AttendanceSummaryReportRow;
import ir.ac.sutech.attendance_system.dto.DailyAttendanceReportRow;
import ir.ac.sutech.attendance_system.dto.DailyAttendanceStatus;

import ir.ac.sutech.attendance_system.model.Employee;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import java.util.stream.Collectors;

@Service
public class AttendanceSummaryReportService {

    private final DailyAttendanceReportService
            dailyAttendanceReportService;

    public AttendanceSummaryReportService(
            DailyAttendanceReportService
                    dailyAttendanceReportService
    ) {

        this.dailyAttendanceReportService =
                dailyAttendanceReportService;
    }

    @Transactional(readOnly = true)
    public List<AttendanceSummaryReportRow>
    generateReport(

            LocalDate fromDate,
            LocalDate toDate,
            Long employeeId
    ) {

        List<DailyAttendanceReportRow>
                dailyRows =
                dailyAttendanceReportService
                        .generateReport(
                                fromDate,
                                toDate,
                                employeeId
                        );

        Map<
                Long,
                List<DailyAttendanceReportRow>
                >
                rowsGroupedByEmployee =

                dailyRows.stream()
                        .collect(

                                Collectors.groupingBy(

                                        row ->
                                                row.getEmployee()
                                                        .getId(),

                                        LinkedHashMap::new,

                                        Collectors.toList()
                                )
                        );

        return rowsGroupedByEmployee
                .values()
                .stream()
                .map(this::createSummaryRow)
                .toList();
    }

    private AttendanceSummaryReportRow
    createSummaryRow(

            List<DailyAttendanceReportRow>
                    employeeRows
    ) {

        Employee employee =
                employeeRows
                        .get(0)
                        .getEmployee();

        long presentDays =
                countStatus(
                        employeeRows,
                        DailyAttendanceStatus.PRESENT
                );

        long absentDays =
                countStatus(
                        employeeRows,
                        DailyAttendanceStatus.ABSENT
                );

        long incompleteDays =
                countStatus(
                        employeeRows,
                        DailyAttendanceStatus.INCOMPLETE
                );

        long leaveDays =
                countStatus(
                        employeeRows,
                        DailyAttendanceStatus.LEAVE
                );

        long missionDays =
                countStatus(
                        employeeRows,
                        DailyAttendanceStatus.MISSION
                );

        long holidayDays =
                countStatus(
                        employeeRows,
                        DailyAttendanceStatus.HOLIDAY
                );

        long noShiftDays =
                countStatus(
                        employeeRows,
                        DailyAttendanceStatus.NO_SHIFT
                );

        long totalWorkedMinutes =
                employeeRows.stream()
                        .mapToLong(
                                DailyAttendanceReportRow
                                        ::getWorkedMinutes
                        )
                        .sum();

        long totalLateMinutes =
                employeeRows.stream()
                        .mapToLong(
                                DailyAttendanceReportRow
                                        ::getLateMinutes
                        )
                        .sum();

        long totalEarlyLeaveMinutes =
                employeeRows.stream()
                        .mapToLong(
                                DailyAttendanceReportRow
                                        ::getEarlyLeaveMinutes
                        )
                        .sum();

        return new AttendanceSummaryReportRow(

                employee,

                presentDays,
                absentDays,
                incompleteDays,

                leaveDays,
                missionDays,
                holidayDays,

                noShiftDays,

                totalWorkedMinutes,
                totalLateMinutes,
                totalEarlyLeaveMinutes
        );
    }

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
}