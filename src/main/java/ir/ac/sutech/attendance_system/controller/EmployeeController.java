package ir.ac.sutech.attendance_system.controller;

import ir.ac.sutech.attendance_system.dto.EmployeeForm;
import ir.ac.sutech.attendance_system.model.EmployeeType;
import ir.ac.sutech.attendance_system.service.EmployeeService;
import jakarta.validation.Valid;
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

@Controller
@RequestMapping("/admin/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @ModelAttribute("employeeTypes")
    public EmployeeType[] employeeTypes() {
        return EmployeeType.values();
    }

    @GetMapping
    public String showEmployeeList(Model model) {

        model.addAttribute(
                "employees",
                employeeService.findAllEmployees()
        );

        model.addAttribute(
                "employeeCount",
                employeeService.countEmployees()
        );

        return "employee-list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {

        if (!model.containsAttribute("employeeForm")) {
            model.addAttribute(
                    "employeeForm",
                    new EmployeeForm()
            );
        }

        addFormOptions(model);
        model.addAttribute("editMode", false);

        return "employee-form";
    }

    @PostMapping
    public String createEmployee(
            @Valid
            @ModelAttribute("employeeForm")
            EmployeeForm employeeForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {

        if (employeeForm.getPersonnelCode() != null
                && !employeeForm.getPersonnelCode().isBlank()
                && employeeService.personnelCodeExists(
                employeeForm.getPersonnelCode()
        )) {

            bindingResult.rejectValue(
                    "personnelCode",
                    "duplicate",
                    "این کد پرسنلی قبلاً ثبت شده است"
            );
        }

        if (employeeForm.getDeviceUserId() != null
                && !employeeForm.getDeviceUserId().isBlank()
                && employeeService.deviceUserIdExists(
                employeeForm.getDeviceUserId()
        )) {

            bindingResult.rejectValue(
                    "deviceUserId",
                    "duplicate",
                    "این شناسه دستگاه قبلاً برای کارمند دیگری ثبت شده است"
            );
        }

        if (employeeForm.getNationalCode() != null
                && !employeeForm.getNationalCode().isBlank()
                && employeeService.nationalCodeExists(
                employeeForm.getNationalCode()
        )) {

            bindingResult.rejectValue(
                    "nationalCode",
                    "duplicate",
                    "این کد ملی قبلاً ثبت شده است"
            );
        }

        validateShift(employeeForm, bindingResult);

        if (bindingResult.hasErrors()) {

            addFormOptions(model);
            model.addAttribute("editMode", false);

            return "employee-form";
        }

        employeeService.createEmployee(employeeForm);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "کارمند جدید با موفقیت ثبت شد."
        );

        return "redirect:/admin/employees";
    }

    @GetMapping("/{employeeId}/edit")
    public String showEditForm(
            @PathVariable Long employeeId,
            Model model
    ) {

        model.addAttribute(
                "employeeForm",
                employeeService.findEmployeeFormById(employeeId)
        );

        addFormOptions(model);

        model.addAttribute("editMode", true);
        model.addAttribute("employeeId", employeeId);

        return "employee-form";
    }

    @PostMapping("/{employeeId}")
    public String updateEmployee(
            @PathVariable Long employeeId,
            @Valid
            @ModelAttribute("employeeForm")
            EmployeeForm employeeForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {

        if (employeeForm.getPersonnelCode() != null
                && !employeeForm.getPersonnelCode().isBlank()
                && employeeService.personnelCodeExistsForAnotherEmployee(
                employeeForm.getPersonnelCode(),
                employeeId
        )) {

            bindingResult.rejectValue(
                    "personnelCode",
                    "duplicate",
                    "این کد پرسنلی متعلق به کارمند دیگری است"
            );
        }

        if (employeeForm.getDeviceUserId() != null
                && !employeeForm.getDeviceUserId().isBlank()
                && employeeService.deviceUserIdExistsForAnotherEmployee(
                employeeForm.getDeviceUserId(),
                employeeId
        )) {

            bindingResult.rejectValue(
                    "deviceUserId",
                    "duplicate",
                    "این شناسه دستگاه متعلق به کارمند دیگری است"
            );
        }

        if (employeeForm.getNationalCode() != null
                && !employeeForm.getNationalCode().isBlank()
                && employeeService.nationalCodeExistsForAnotherEmployee(
                employeeForm.getNationalCode(),
                employeeId
        )) {

            bindingResult.rejectValue(
                    "nationalCode",
                    "duplicate",
                    "این کد ملی متعلق به کارمند دیگری است"
            );
        }

        validateShift(employeeForm, bindingResult);

        if (bindingResult.hasErrors()) {

            addFormOptions(model);

            model.addAttribute("editMode", true);
            model.addAttribute("employeeId", employeeId);

            return "employee-form";
        }

        employeeService.updateEmployee(
                employeeId,
                employeeForm
        );

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "اطلاعات کارمند با موفقیت ویرایش شد."
        );

        return "redirect:/admin/employees";
    }

    @PostMapping("/{employeeId}/status")
    public String updateEmployeeStatus(
            @PathVariable Long employeeId,
            @RequestParam("active") boolean active,
            RedirectAttributes redirectAttributes
    ) {

        employeeService.updateEmployeeStatus(
                employeeId,
                active
        );

        String message = active
                ? "کارمند با موفقیت فعال شد."
                : "کارمند با موفقیت غیرفعال شد.";

        redirectAttributes.addFlashAttribute(
                "successMessage",
                message
        );

        return "redirect:/admin/employees";
    }

    private void addFormOptions(Model model) {

        model.addAttribute(
                "departments",
                employeeService.findActiveDepartments()
        );

        model.addAttribute(
                "shifts",
                employeeService.findActiveShifts()
        );
    }

    private void validateShift(
            EmployeeForm employeeForm,
            BindingResult bindingResult
    ) {

        if (employeeForm.getEmployeeType()
                == EmployeeType.SERVICE_SUPPORT
                && employeeForm.getShiftId() == null) {

            bindingResult.rejectValue(
                    "shiftId",
                    "required",
                    "برای خدمات و پشتیبانی، انتخاب شیفت کاری الزامی است"
            );
        }

        if (employeeForm.getEmployeeType()
                == EmployeeType.ADMINISTRATIVE) {

            employeeForm.setShiftId(null);
        }
    }
}