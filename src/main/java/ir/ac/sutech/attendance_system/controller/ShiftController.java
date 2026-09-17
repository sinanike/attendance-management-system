package ir.ac.sutech.attendance_system.controller;

import ir.ac.sutech.attendance_system.dto.ShiftForm;
import ir.ac.sutech.attendance_system.model.WorkDay;
import ir.ac.sutech.attendance_system.service.ShiftService;
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
@RequestMapping("/admin/shifts")
public class ShiftController {

    private final ShiftService shiftService;

    public ShiftController(
            ShiftService shiftService
    ) {
        this.shiftService = shiftService;
    }

    @ModelAttribute("workDays")
    public WorkDay[] provideWorkDays() {
        return WorkDay.values();
    }

    @GetMapping
    public String showShiftList(
            Model model
    ) {
        model.addAttribute(
                "shifts",
                shiftService.findAllShifts()
        );

        model.addAttribute(
                "shiftCount",
                shiftService.countShifts()
        );

        return "shift-list";
    }

    @GetMapping("/new")
    public String showCreateForm(
            Model model
    ) {
        model.addAttribute(
                "shiftForm",
                new ShiftForm()
        );

        model.addAttribute(
                "editing",
                false
        );

        return "shift-form";
    }

    @PostMapping
    public String createShift(
            @Valid
            @ModelAttribute("shiftForm")
            ShiftForm shiftForm,

            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        validateShiftTimes(
                shiftForm,
                bindingResult
        );

        if (shiftService.shiftCodeExists(
                shiftForm.getShiftCode()
        )) {
            bindingResult.rejectValue(
                    "shiftCode",
                    "duplicate",
                    "این کد قبلاً برای شیفت دیگری ثبت شده است"
            );
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute(
                    "editing",
                    false
            );

            return "shift-form";
        }

        shiftService.createShift(shiftForm);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "شیفت جدید با موفقیت ثبت شد."
        );

        return "redirect:/admin/shifts";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(
            @PathVariable("id")
            Long shiftId,
            Model model
    ) {
        model.addAttribute(
                "shiftForm",
                shiftService.findShiftFormById(
                        shiftId
                )
        );

        model.addAttribute(
                "shiftId",
                shiftId
        );

        model.addAttribute(
                "editing",
                true
        );

        return "shift-form";
    }

    @PostMapping("/{id}")
    public String updateShift(
            @PathVariable("id")
            Long shiftId,

            @Valid
            @ModelAttribute("shiftForm")
            ShiftForm shiftForm,

            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        validateShiftTimes(
                shiftForm,
                bindingResult
        );

        if (shiftService
                .shiftCodeExistsForAnotherShift(
                        shiftForm.getShiftCode(),
                        shiftId
                )) {

            bindingResult.rejectValue(
                    "shiftCode",
                    "duplicate",
                    "این کد قبلاً برای شیفت دیگری ثبت شده است"
            );
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute(
                    "shiftId",
                    shiftId
            );

            model.addAttribute(
                    "editing",
                    true
            );

            return "shift-form";
        }

        shiftService.updateShift(
                shiftId,
                shiftForm
        );

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "اطلاعات شیفت با موفقیت ویرایش شد."
        );

        return "redirect:/admin/shifts";
    }

    @PostMapping("/{id}/status")
    public String updateShiftStatus(
            @PathVariable("id")
            Long shiftId,

            @RequestParam("active")
            boolean active,

            RedirectAttributes redirectAttributes
    ) {
        shiftService.updateShiftStatus(
                shiftId,
                active
        );

        String message = active
                ? "شیفت با موفقیت فعال شد."
                : "شیفت با موفقیت غیرفعال شد.";

        redirectAttributes.addFlashAttribute(
                "successMessage",
                message
        );

        return "redirect:/admin/shifts";
    }

    private void validateShiftTimes(
            ShiftForm shiftForm,
            BindingResult bindingResult
    ) {
        if (shiftForm.getStartTime() == null
                || shiftForm.getEndTime() == null) {
            return;
        }

        if (shiftForm.getStartTime().equals(
                shiftForm.getEndTime()
        )) {
            bindingResult.rejectValue(
                    "endTime",
                    "sameTime",
                    "ساعت پایان نمی‌تواند با ساعت شروع یکسان باشد"
            );
        }
    }
}