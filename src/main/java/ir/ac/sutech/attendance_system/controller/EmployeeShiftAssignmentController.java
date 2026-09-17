package ir.ac.sutech.attendance_system.controller;

import ir.ac.sutech.attendance_system.dto.EmployeeShiftAssignmentForm;
import ir.ac.sutech.attendance_system.model.EmployeeType;
import ir.ac.sutech.attendance_system.service.EmployeeService;
import ir.ac.sutech.attendance_system.service.EmployeeShiftAssignmentService;
import ir.ac.sutech.attendance_system.service.ShiftService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/admin/shift-assignments")
public class EmployeeShiftAssignmentController {

    private final EmployeeShiftAssignmentService
            assignmentService;

    private final EmployeeService employeeService;

    private final ShiftService shiftService;

    public EmployeeShiftAssignmentController(
            EmployeeShiftAssignmentService
                    assignmentService,

            EmployeeService employeeService,

            ShiftService shiftService
    ) {
        this.assignmentService =
                assignmentService;

        this.employeeService =
                employeeService;

        this.shiftService =
                shiftService;
    }

    @GetMapping
    public String showAssignments(
            Model model
    ) {

        if (
                !model.containsAttribute(
                        "assignmentForm"
                )
        ) {

            EmployeeShiftAssignmentForm form =
                    new EmployeeShiftAssignmentForm();

            /*
             * پیش‌فرض:
             * تخصیص برای امروز
             */
            form.setStartDate(
                    LocalDate.now()
            );

            form.setEndDate(
                    LocalDate.now()
            );

            model.addAttribute(
                    "assignmentForm",
                    form
            );
        }

        addPageData(model);

        return "employee-shift-assignment-list";
    }

    @PostMapping
    public String createAssignment(
            @Valid
            @ModelAttribute("assignmentForm")
            EmployeeShiftAssignmentForm form,

            BindingResult bindingResult,

            Model model,

            RedirectAttributes redirectAttributes
    ) {

        /*
         * اعتبارسنجی تاریخ پایان
         */
        if (
                form.getStartDate() != null
                        && form.getEndDate() != null
                        && form.getEndDate()
                        .isBefore(
                                form.getStartDate()
                        )
        ) {

            bindingResult.rejectValue(
                    "endDate",
                    "invalidDateRange",
                    "تاریخ پایان نمی‌تواند قبل از تاریخ شروع باشد."
            );
        }

        if (bindingResult.hasErrors()) {

            addPageData(model);

            return "employee-shift-assignment-list";
        }

        try {

            assignmentService.assignShift(
                    form.getEmployeeId(),
                    form.getShiftId(),
                    form.getStartDate(),
                    form.getEndDate()
            );

        } catch (
                IllegalArgumentException
                | IllegalStateException
                | EntityNotFoundException exception
        ) {

            model.addAttribute(
                    "errorMessage",
                    exception.getMessage()
            );

            addPageData(model);

            return "employee-shift-assignment-list";
        }

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "شیفت با موفقیت برای کارمند ثبت شد."
        );

        return "redirect:/admin/shift-assignments";
    }

    /*
     * لغو Assignment ثبت‌شده اشتباه.
     */
    @PostMapping("/{assignmentId}/cancel")
    public String cancelAssignment(
            @PathVariable("assignmentId")
            Long assignmentId,

            RedirectAttributes redirectAttributes
    ) {

        try {

            assignmentService.cancelAssignment(
                    assignmentId
            );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "تخصیص شیفت با موفقیت لغو شد."
            );

        } catch (
                IllegalStateException
                | EntityNotFoundException exception
        ) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    exception.getMessage()
            );
        }

        return "redirect:/admin/shift-assignments";
    }

    private void addPageData(
            Model model
    ) {

        /*
         * در صفحه تخصیص شیفت فقط کارکنان
         * خدمات و پشتیبانی نمایش داده شوند.
         */
        model.addAttribute(
                "employees",
                employeeService
                        .findAllEmployees()
                        .stream()
                        .filter(employee ->
                                employee.isActive()
                        )
                        .filter(employee ->
                                employee.getEmployeeType()
                                        == EmployeeType.SERVICE_SUPPORT
                        )
                        .toList()
        );

        model.addAttribute(
                "shifts",
                shiftService.findActiveShifts()
        );

        model.addAttribute(
                "assignments",
                assignmentService
                        .findAllAssignments()
        );

        model.addAttribute(
                "assignmentCount",
                assignmentService
                        .countAssignments()
        );

        model.addAttribute(
                "today",
                LocalDate.now()
        );
    }
}