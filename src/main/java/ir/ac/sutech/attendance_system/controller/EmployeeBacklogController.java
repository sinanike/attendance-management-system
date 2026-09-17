package ir.ac.sutech.attendance_system.controller;

import ir.ac.sutech.attendance_system.dto.AttendanceMetricRow;
import ir.ac.sutech.attendance_system.dto.LeaveBalanceView;
import ir.ac.sutech.attendance_system.model.Employee;
import ir.ac.sutech.attendance_system.model.EmployeeType;
import ir.ac.sutech.attendance_system.service.BacklogAttendanceMetricsService;
import ir.ac.sutech.attendance_system.service.CurrentEmployeeService;
import ir.ac.sutech.attendance_system.service.LeaveBalanceService;
import ir.ac.sutech.attendance_system.util.JalaliDateUtil;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.time.LocalDate;

@Controller
@RequestMapping("/employee")
public class EmployeeBacklogController {

    private final CurrentEmployeeService currentEmployeeService;
    private final LeaveBalanceService leaveBalanceService;
    private final BacklogAttendanceMetricsService attendanceMetricsService;
    private final JalaliDateUtil jalaliDateUtil;

    public EmployeeBacklogController(
            CurrentEmployeeService currentEmployeeService,
            LeaveBalanceService leaveBalanceService,
            BacklogAttendanceMetricsService attendanceMetricsService,
            JalaliDateUtil jalaliDateUtil
    ) {
        this.currentEmployeeService = currentEmployeeService;
        this.leaveBalanceService = leaveBalanceService;
        this.attendanceMetricsService = attendanceMetricsService;
        this.jalaliDateUtil = jalaliDateUtil;
    }

    @GetMapping("/leave-balance")
    public String leaveBalance(
            @RequestParam(required = false) Integer year,
            Principal principal,
            Model model
    ) {
        int selectedYear = year == null
                ? jalaliDateUtil.currentJalaliYear()
                : year;

        Employee employee = currentEmployeeService
                .requireEmployee(principal.getName());

        LeaveBalanceView balance = leaveBalanceService
                .getBalance(employee, selectedYear);

        model.addAttribute("employee", employee);
        model.addAttribute("year", selectedYear);
        model.addAttribute("balance", balance);

        return "employee-leave-balance";
    }

    @GetMapping("/overtime")
    public String overtime(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate toDate,
            Principal principal,
            Model model
    ) {
        LocalDate[] currentJalaliMonth =
                jalaliDateUtil.currentJalaliMonthRange();

        if (fromDate == null) {
            fromDate = currentJalaliMonth[0];
        }
        if (toDate == null) {
            toDate = currentJalaliMonth[1];
        }

        Employee employee = currentEmployeeService
                .requireEmployee(principal.getName());

        AttendanceMetricRow row = attendanceMetricsService
                .getEmployeeMetrics(
                        employee.getId(),
                        fromDate,
                        toDate
                );

        model.addAttribute("employee", employee);
        model.addAttribute("serviceEmployee",
                employee.getEmployeeType()
                        == EmployeeType.SERVICE_SUPPORT);
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        model.addAttribute("row", row);

        return "employee-overtime";
    }
}
