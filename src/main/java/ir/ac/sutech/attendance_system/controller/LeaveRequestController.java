package ir.ac.sutech.attendance_system.controller;

import ir.ac.sutech.attendance_system.model.LeaveRequest;
import ir.ac.sutech.attendance_system.model.LeaveStatus;
import ir.ac.sutech.attendance_system.service.LeaveRequestService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/leaves")
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;


    public LeaveRequestController(
            LeaveRequestService leaveRequestService
    ) {

        this.leaveRequestService =
                leaveRequestService;
    }


    /*
     * ==========================================
     * صفحه مدیریت درخواست‌های مرخصی
     * ==========================================
     */

    @GetMapping
    public String showLeaveRequests(
            @RequestParam(required = false)
            String status,

            Model model
    ) {

        List<LeaveRequest> allRequests =
                leaveRequestService
                        .findAllRequests();


        /*
         * آمار درخواست‌ها
         */

        long pendingCount =
                allRequests.stream()
                        .filter(LeaveRequest::isPending)
                        .count();


        long approvedCount =
                allRequests.stream()
                        .filter(LeaveRequest::isApproved)
                        .count();


        long rejectedCount =
                allRequests.stream()
                        .filter(LeaveRequest::isRejected)
                        .count();


        /*
         * فیلتر اختیاری وضعیت
         */

        List<LeaveRequest> requests =
                filterByStatus(
                        allRequests,
                        status
                );


        model.addAttribute(
                "leaveRequests",
                requests
        );

        model.addAttribute(
                "totalCount",
                allRequests.size()
        );

        model.addAttribute(
                "pendingCount",
                pendingCount
        );

        model.addAttribute(
                "approvedCount",
                approvedCount
        );

        model.addAttribute(
                "rejectedCount",
                rejectedCount
        );

        model.addAttribute(
                "selectedStatus",
                status
        );


        return "leave-request-list";
    }


    /*
     * ==========================================
     * تأیید درخواست
     * ==========================================
     */

    @PostMapping("/{id}/approve")
    public String approve(
            @PathVariable
            Long id,

            @RequestParam(
                    required = false
            )
            String reviewNote,

            RedirectAttributes redirectAttributes
    ) {

        try {

            leaveRequestService
                    .approveRequest(
                            id,
                            reviewNote
                    );


            redirectAttributes
                    .addFlashAttribute(
                            "successMessage",
                            "درخواست مرخصی با موفقیت تأیید شد."
                    );

        } catch (
                IllegalArgumentException exception
        ) {

            redirectAttributes
                    .addFlashAttribute(
                            "errorMessage",
                            exception.getMessage()
                    );
        }


        return "redirect:/admin/leaves";
    }


    /*
     * ==========================================
     * رد درخواست
     * ==========================================
     */

    @PostMapping("/{id}/reject")
    public String reject(
            @PathVariable
            Long id,

            @RequestParam(
                    required = false
            )
            String reviewNote,

            RedirectAttributes redirectAttributes
    ) {

        try {

            leaveRequestService
                    .rejectRequest(
                            id,
                            reviewNote
                    );


            redirectAttributes
                    .addFlashAttribute(
                            "successMessage",
                            "درخواست مرخصی رد شد."
                    );

        } catch (
                IllegalArgumentException exception
        ) {

            redirectAttributes
                    .addFlashAttribute(
                            "errorMessage",
                            exception.getMessage()
                    );
        }


        return "redirect:/admin/leaves";
    }


    /*
     * ==========================================
     * بازگرداندن برای بررسی مجدد
     * ==========================================
     */

    @PostMapping("/{id}/pending")
    public String markPending(
            @PathVariable
            Long id,

            RedirectAttributes redirectAttributes
    ) {

        try {

            leaveRequestService
                    .markPending(id);


            redirectAttributes
                    .addFlashAttribute(
                            "successMessage",
                            "درخواست برای بررسی مجدد بازگردانده شد."
                    );

        } catch (
                IllegalArgumentException exception
        ) {

            redirectAttributes
                    .addFlashAttribute(
                            "errorMessage",
                            exception.getMessage()
                    );
        }


        return "redirect:/admin/leaves";
    }


    /*
     * ==========================================
     * فیلتر وضعیت
     * ==========================================
     */

    private List<LeaveRequest> filterByStatus(
            List<LeaveRequest> requests,
            String status
    ) {

        if (
                status == null
                        ||
                        status.isBlank()
                        ||
                        status.equalsIgnoreCase("ALL")
        ) {

            return requests;
        }


        LeaveStatus leaveStatus;

        try {

            leaveStatus =
                    LeaveStatus.valueOf(
                            status.toUpperCase()
                    );

        } catch (
                IllegalArgumentException exception
        ) {

            return requests;
        }


        return requests.stream()
                .filter(request ->
                        request.getStatus()
                                == leaveStatus
                )
                .toList();
    }
}