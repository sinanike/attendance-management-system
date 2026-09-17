package ir.ac.sutech.attendance_system.controller;

import ir.ac.sutech.attendance_system.model.MissionRequest;
import ir.ac.sutech.attendance_system.service.MissionRequestService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/missions")
public class MissionRequestController {

    private final MissionRequestService
            missionRequestService;

    public MissionRequestController(
            MissionRequestService missionRequestService
    ) {
        this.missionRequestService =
                missionRequestService;
    }

    @GetMapping
    public String showMissionRequests(

            @RequestParam(
                    required = false,
                    defaultValue = "ALL"
            )
            String status,

            Model model
    ) {

        List<MissionRequest> allRequests =
                missionRequestService
                        .findAllRequests();

        String selectedStatus =
                normalizeStatus(status);

        List<MissionRequest> filteredRequests =
                filterByStatus(
                        allRequests,
                        selectedStatus
                );

        long pendingCount =
                allRequests.stream()
                        .filter(
                                MissionRequest::isPending
                        )
                        .count();

        long approvedCount =
                allRequests.stream()
                        .filter(
                                MissionRequest::isApproved
                        )
                        .count();

        long rejectedCount =
                allRequests.stream()
                        .filter(
                                MissionRequest::isRejected
                        )
                        .count();

        model.addAttribute(
                "missionRequests",
                filteredRequests
        );

        model.addAttribute(
                "selectedStatus",
                selectedStatus
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

        return "mission-request-list";
    }

    @PostMapping("/{id}/approve")
    public String approveMission(

            @PathVariable
            Long id,

            @RequestParam(required = false)
            String reviewNote,

            RedirectAttributes redirectAttributes
    ) {

        try {

            missionRequestService
                    .approveRequest(
                            id,
                            reviewNote
                    );

            redirectAttributes
                    .addFlashAttribute(
                            "successMessage",
                            "درخواست مأموریت با موفقیت تأیید شد."
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

        return "redirect:/admin/missions?status=PENDING";
    }

    @PostMapping("/{id}/reject")
    public String rejectMission(

            @PathVariable
            Long id,

            @RequestParam(required = false)
            String reviewNote,

            RedirectAttributes redirectAttributes
    ) {

        try {

            missionRequestService
                    .rejectRequest(
                            id,
                            reviewNote
                    );

            redirectAttributes
                    .addFlashAttribute(
                            "successMessage",
                            "درخواست مأموریت رد شد."
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

        return "redirect:/admin/missions?status=PENDING";
    }

    @PostMapping("/{id}/pending")
    public String markMissionPending(

            @PathVariable
            Long id,

            RedirectAttributes redirectAttributes
    ) {

        try {

            missionRequestService
                    .markPending(id);

            redirectAttributes
                    .addFlashAttribute(
                            "successMessage",
                            "درخواست مأموریت برای بررسی مجدد بازگردانده شد."
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

        return "redirect:/admin/missions";
    }

    private List<MissionRequest> filterByStatus(
            List<MissionRequest> requests,
            String status
    ) {

        return switch (status) {

            case "PENDING" ->
                    requests.stream()
                            .filter(
                                    MissionRequest::isPending
                            )
                            .toList();

            case "APPROVED" ->
                    requests.stream()
                            .filter(
                                    MissionRequest::isApproved
                            )
                            .toList();

            case "REJECTED" ->
                    requests.stream()
                            .filter(
                                    MissionRequest::isRejected
                            )
                            .toList();

            default ->
                    requests;
        };
    }

    private String normalizeStatus(
            String status
    ) {

        if (status == null) {
            return "ALL";
        }

        String normalized =
                status.trim()
                        .toUpperCase();

        return switch (normalized) {

            case "PENDING",
                 "APPROVED",
                 "REJECTED" ->
                    normalized;

            default ->
                    "ALL";
        };
    }
}