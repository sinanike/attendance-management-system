package ir.ac.sutech.attendance_system.controller;

import ir.ac.sutech.attendance_system.service.OfficialHolidayService;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
@RequestMapping("/admin/holidays")
public class OfficialHolidayController {

    private final OfficialHolidayService
            holidayService;


    public OfficialHolidayController(
            OfficialHolidayService holidayService
    ) {
        this.holidayService =
                holidayService;
    }


    @GetMapping
    public String showPage(
            Model model
    ) {

        model.addAttribute(
                "holidays",
                holidayService.findAll()
        );

        model.addAttribute(
                "today",
                LocalDate.now()
        );

        return "official-holiday-list";
    }


    @PostMapping
    public String create(
            @RequestParam
            @DateTimeFormat(
                    iso =
                            DateTimeFormat.ISO.DATE
            )
            LocalDate holidayDate,

            @RequestParam
            String title,

            @RequestParam(
                    required = false
            )
            String description,

            Model model
    ) {

        try {

            holidayService.create(
                    holidayDate,
                    title,
                    description
            );

        } catch (
                IllegalArgumentException exception
        ) {

            model.addAttribute(
                    "errorMessage",
                    exception.getMessage()
            );

            model.addAttribute(
                    "holidays",
                    holidayService.findAll()
            );

            model.addAttribute(
                    "today",
                    holidayDate
            );

            return "official-holiday-list";
        }

        return "redirect:/admin/holidays";
    }


    @PostMapping("/{id}/toggle")
    public String toggle(
            @PathVariable
            Long id
    ) {

        holidayService.toggle(id);

        return "redirect:/admin/holidays";
    }
}