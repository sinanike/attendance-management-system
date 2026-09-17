package ir.ac.sutech.attendance_system.controller;

import ir.ac.sutech.attendance_system.dto.DailyAttendanceReportRow;
import ir.ac.sutech.attendance_system.model.AttendanceRecord;
import ir.ac.sutech.attendance_system.model.AttendanceType;
import ir.ac.sutech.attendance_system.repository.EmployeeRepository;
import ir.ac.sutech.attendance_system.service.AttendanceRecordService;
import ir.ac.sutech.attendance_system.service.DailyAttendanceReportService;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/admin/reports")
public class AttendanceReportController {

    private final AttendanceRecordService attendanceRecordService;
    private final DailyAttendanceReportService dailyAttendanceReportService;
    private final EmployeeRepository employeeRepository;

    public AttendanceReportController(
            AttendanceRecordService attendanceRecordService,
            DailyAttendanceReportService dailyAttendanceReportService,
            EmployeeRepository employeeRepository
    ) {
        this.attendanceRecordService = attendanceRecordService;
        this.dailyAttendanceReportService =
                dailyAttendanceReportService;
        this.employeeRepository = employeeRepository;
    }

    @GetMapping("/attendance")
    public String showAttendanceReport(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fromDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate toDate,

            @RequestParam(required = false)
            Long employeeId,

            Model model
    ) {
        LocalDate today = LocalDate.now();

        if (fromDate == null) {
            fromDate = today;
        }

        if (toDate == null) {
            toDate = today;
        }

        List<AttendanceRecord> attendanceRecords = List.of();
        List<DailyAttendanceReportRow> dailyReportRows = List.of();

        try {
            attendanceRecords =
                    attendanceRecordService.findRecordsForReport(
                            fromDate,
                            toDate,
                            employeeId
                    );

            dailyReportRows =
                    dailyAttendanceReportService.generateReport(
                            fromDate,
                            toDate,
                            employeeId
                    );

        } catch (IllegalArgumentException exception) {
            model.addAttribute(
                    "reportError",
                    exception.getMessage()
            );
        }

        long checkInCount = attendanceRecords.stream()
                .filter(record ->
                        record.getAttendanceType()
                                == AttendanceType.ENTRY
                )
                .count();

        long checkOutCount = attendanceRecords.stream()
                .filter(record ->
                        record.getAttendanceType()
                                == AttendanceType.EXIT
                )
                .count();

        model.addAttribute(
                "attendanceRecords",
                attendanceRecords
        );

        model.addAttribute(
                "dailyReportRows",
                dailyReportRows
        );

        model.addAttribute(
                "employees",
                employeeRepository
                        .findByActiveTrueOrderByLastNameAscFirstNameAsc()
        );

        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);

        model.addAttribute(
                "selectedEmployeeId",
                employeeId
        );

        model.addAttribute(
                "recordCount",
                attendanceRecords.size()
        );

        model.addAttribute(
                "checkInCount",
                checkInCount
        );

        model.addAttribute(
                "checkOutCount",
                checkOutCount
        );

        return "attendance-report";
    }
}