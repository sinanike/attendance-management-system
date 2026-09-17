package ir.ac.sutech.attendance_system.controller;

import ir.ac.sutech.attendance_system.dto.ShiftWeeklyScheduleForm;
import ir.ac.sutech.attendance_system.service.ShiftService;
import ir.ac.sutech.attendance_system.service.ShiftWeeklyScheduleService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ShiftWeeklyScheduleController {

    private final ShiftWeeklyScheduleService scheduleService;
    private final ShiftService shiftService;

    public ShiftWeeklyScheduleController(
            ShiftWeeklyScheduleService scheduleService,
            ShiftService shiftService
    ) {
        this.scheduleService = scheduleService;
        this.shiftService = shiftService;
    }

    @GetMapping("/admin/shifts/{id}/weekly-schedule")
    public String show(
            @PathVariable("id") Long shiftId,
            Model model
    ) {
        model.addAttribute(
                "shift",
                shiftService.findShiftById(shiftId)
        );

        model.addAttribute(
                "scheduleForm",
                scheduleService.getForm(shiftId)
        );

        return "shift-weekly-schedule";
    }

    @PostMapping("/admin/shifts/{id}/weekly-schedule")
    public String save(
            @PathVariable("id") Long shiftId,
            @ModelAttribute("scheduleForm")
            ShiftWeeklyScheduleForm form,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        try {
            scheduleService.save(shiftId, form);
        } catch (IllegalArgumentException exception) {
            model.addAttribute(
                    "shift",
                    shiftService.findShiftById(shiftId)
            );

            model.addAttribute(
                    "errorMessage",
                    exception.getMessage()
            );

            return "shift-weekly-schedule";
        }

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "برنامه هفتگی شیفت با موفقیت ذخیره شد."
        );

        return "redirect:/admin/shifts/"
                + shiftId
                + "/weekly-schedule";
    }
}