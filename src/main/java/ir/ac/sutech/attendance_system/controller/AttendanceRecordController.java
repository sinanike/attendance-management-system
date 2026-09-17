package ir.ac.sutech.attendance_system.controller;

import ir.ac.sutech.attendance_system.dto.AttendanceRecordForm;
import ir.ac.sutech.attendance_system.model.AttendanceType;
import ir.ac.sutech.attendance_system.service.AttendanceImportService;
import ir.ac.sutech.attendance_system.service.AttendanceRecordService;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.validation.BindingResult;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.multipart.MultipartFile;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AttendanceRecordController {

    private final AttendanceRecordService
            attendanceRecordService;

    private final AttendanceImportService
            attendanceImportService;

    public AttendanceRecordController(
            AttendanceRecordService attendanceRecordService,
            AttendanceImportService attendanceImportService
    ) {
        this.attendanceRecordService =
                attendanceRecordService;

        this.attendanceImportService =
                attendanceImportService;
    }

    @GetMapping("/admin/attendance")
    public String showAttendanceList(Model model) {

        var attendanceRecords =
                attendanceRecordService.findAllRecords();

        model.addAttribute(
                "attendanceRecords",
                attendanceRecords
        );

        model.addAttribute(
                "recordCount",
                attendanceRecords.size()
        );

        return "attendance-list";
    }

    @GetMapping("/admin/attendance/new")
    public String showCreateForm(Model model) {

        if (!model.containsAttribute(
                "attendanceRecordForm"
        )) {
            model.addAttribute(
                    "attendanceRecordForm",
                    new AttendanceRecordForm()
            );
        }

        prepareFormData(model);

        return "attendance-form";
    }

    @PostMapping("/admin/attendance")
    public String createRecord(
            @Valid
            @ModelAttribute("attendanceRecordForm")
            AttendanceRecordForm form,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {

        if (bindingResult.hasErrors()) {
            prepareFormData(model);
            return "attendance-form";
        }

        try {
            attendanceRecordService
                    .createManualRecord(form);

        } catch (IllegalArgumentException exception) {

            bindingResult.reject(
                    "attendance.error",
                    exception.getMessage()
            );

            prepareFormData(model);

            return "attendance-form";
        }

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "تردد با موفقیت ثبت شد."
        );

        return "redirect:/admin/attendance";
    }

    @PostMapping(
            "/admin/attendance/{recordId}/delete"
    )
    public String deleteRecord(
            @PathVariable Long recordId,
            RedirectAttributes redirectAttributes
    ) {

        try {
            attendanceRecordService
                    .deleteRecord(recordId);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "تردد با موفقیت حذف شد."
            );

        } catch (IllegalArgumentException exception) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    exception.getMessage()
            );
        }

        return "redirect:/admin/attendance";
    }

    @PostMapping("/admin/attendance/import")
    public String importDeviceFile(
            @RequestParam("file")
            MultipartFile file,

            @RequestParam("deviceCode")
            String deviceCode,

            RedirectAttributes redirectAttributes
    ) {

        try {
            var result =
                    attendanceImportService.importCsv(
                            file,
                            deviceCode
                    );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    result.getSummary()
            );

            redirectAttributes.addFlashAttribute(
                    "importErrors",
                    result.errors()
            );

        } catch (IllegalArgumentException exception) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    exception.getMessage()
            );
        }

        return "redirect:/admin/attendance";
    }

    private void prepareFormData(Model model) {

        model.addAttribute(
                "employees",
                attendanceRecordService
                        .findActiveEmployees()
        );

        model.addAttribute(
                "attendanceTypes",
                AttendanceType.values()
        );
    }
}