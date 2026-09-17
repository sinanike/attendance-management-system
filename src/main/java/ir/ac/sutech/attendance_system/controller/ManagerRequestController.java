package ir.ac.sutech.attendance_system.controller;

import ir.ac.sutech.attendance_system.model.Employee;

import ir.ac.sutech.attendance_system.model.LeaveRequest;
import ir.ac.sutech.attendance_system.model.LeaveStatus;

import ir.ac.sutech.attendance_system.model.MissionRequest;
import ir.ac.sutech.attendance_system.model.MissionStatus;

import ir.ac.sutech.attendance_system.repository.LeaveRequestRepository;
import ir.ac.sutech.attendance_system.repository.MissionRequestRepository;

import ir.ac.sutech.attendance_system.service.CurrentManagerService;
import ir.ac.sutech.attendance_system.service.LeaveRequestService;
import ir.ac.sutech.attendance_system.service.MissionRequestService;

import org.springframework.security.core.Authentication;

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
@RequestMapping("/manager")
public class ManagerRequestController {


    private final CurrentManagerService
            currentManagerService;


    private final LeaveRequestRepository
            leaveRequestRepository;


    private final MissionRequestRepository
            missionRequestRepository;


    private final LeaveRequestService
            leaveRequestService;


    private final MissionRequestService
            missionRequestService;


    public ManagerRequestController(

            CurrentManagerService currentManagerService,

            LeaveRequestRepository leaveRequestRepository,

            MissionRequestRepository missionRequestRepository,

            LeaveRequestService leaveRequestService,

            MissionRequestService missionRequestService

    ) {


        this.currentManagerService =
                currentManagerService;


        this.leaveRequestRepository =
                leaveRequestRepository;


        this.missionRequestRepository =
                missionRequestRepository;


        this.leaveRequestService =
                leaveRequestService;


        this.missionRequestService =
                missionRequestService;
    }


    /*
     * =====================================================
     * مرخصی‌های کارکنان واحد
     * =====================================================
     */

    @GetMapping("/leaves")
    public String leaves(

            @RequestParam(
                    defaultValue = "ALL"
            )
            String status,

            Authentication authentication,

            Model model

    ) {


        Employee manager =
                currentManagerService
                        .getCurrentManager(
                                authentication
                        );


        Long departmentId =
                manager
                        .getDepartment()
                        .getId();


        String selectedStatus =
                normalizeStatus(
                        status
                );


        /*
         * تمام درخواست‌های همین Department
         *
         * دیگر کل جدول DB خوانده نمی‌شود.
         */

        List<LeaveRequest> allRequests =
                leaveRequestRepository
                        .findByEmployeeDepartmentIdOrderByCreatedAtDescIdDesc(
                                departmentId
                        );


        List<LeaveRequest> filteredRequests;


        if (
                selectedStatus.equals(
                        "PENDING"
                )
        ) {


            filteredRequests =
                    leaveRequestRepository
                            .findByEmployeeDepartmentIdAndStatusOrderByCreatedAtDescIdDesc(

                                    departmentId,

                                    LeaveStatus.PENDING
                            );


        } else if (
                selectedStatus.equals(
                        "APPROVED"
                )
        ) {


            filteredRequests =
                    leaveRequestRepository
                            .findByEmployeeDepartmentIdAndStatusOrderByCreatedAtDescIdDesc(

                                    departmentId,

                                    LeaveStatus.APPROVED
                            );


        } else if (
                selectedStatus.equals(
                        "REJECTED"
                )
        ) {


            filteredRequests =
                    leaveRequestRepository
                            .findByEmployeeDepartmentIdAndStatusOrderByCreatedAtDescIdDesc(

                                    departmentId,

                                    LeaveStatus.REJECTED
                            );


        } else {


            filteredRequests =
                    allRequests;
        }


        long pendingCount =
                allRequests
                        .stream()
                        .filter(
                                LeaveRequest::isPending
                        )
                        .count();


        long approvedCount =
                allRequests
                        .stream()
                        .filter(
                                LeaveRequest::isApproved
                        )
                        .count();


        long rejectedCount =
                allRequests
                        .stream()
                        .filter(
                                LeaveRequest::isRejected
                        )
                        .count();


        model.addAttribute(
                "manager",
                manager
        );


        model.addAttribute(
                "department",
                manager.getDepartment()
        );


        model.addAttribute(
                "leaveRequests",
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


        return "manager-leaves";
    }


    /*
     * =====================================================
     * تأیید مرخصی
     * =====================================================
     */

    @PostMapping("/leaves/{id}/approve")
    public String approveLeave(

            @PathVariable Long id,

            @RequestParam(required = false)
            String reviewNote,

            Authentication authentication,

            RedirectAttributes redirectAttributes

    ) {


        try {


            Employee manager =
                    currentManagerService
                            .getCurrentManager(
                                    authentication
                            );


            LeaveRequest request =
                    getManagerLeaveRequest(
                            id,
                            manager
                    );


            /*
             * مدیر نباید درخواست خودش را تأیید کند.
             */

            ensureNotOwnRequest(

                    manager,

                    request
                            .getEmployee()
                            .getId()
            );


            if (!request.isPending()) {

                throw new IllegalStateException(
                        "فقط درخواست در انتظار بررسی قابل تأیید است."
                );
            }


            leaveRequestService
                    .approveRequest(
                            request.getId(),
                            reviewNote
                    );


            redirectAttributes
                    .addFlashAttribute(

                            "successMessage",

                            "درخواست مرخصی با موفقیت تأیید شد."
                    );


        } catch (
                IllegalArgumentException
                |
                IllegalStateException exception
        ) {


            redirectAttributes
                    .addFlashAttribute(

                            "errorMessage",

                            exception.getMessage()
                    );
        }


        return "redirect:/manager/leaves?status=PENDING";
    }


    /*
     * =====================================================
     * رد مرخصی
     * =====================================================
     */

    @PostMapping("/leaves/{id}/reject")
    public String rejectLeave(

            @PathVariable Long id,

            @RequestParam(required = false)
            String reviewNote,

            Authentication authentication,

            RedirectAttributes redirectAttributes

    ) {


        try {


            Employee manager =
                    currentManagerService
                            .getCurrentManager(
                                    authentication
                            );


            LeaveRequest request =
                    getManagerLeaveRequest(
                            id,
                            manager
                    );


            ensureNotOwnRequest(

                    manager,

                    request
                            .getEmployee()
                            .getId()
            );


            if (!request.isPending()) {

                throw new IllegalStateException(
                        "فقط درخواست در انتظار بررسی قابل رد است."
                );
            }


            leaveRequestService
                    .rejectRequest(
                            request.getId(),
                            reviewNote
                    );


            redirectAttributes
                    .addFlashAttribute(

                            "successMessage",

                            "درخواست مرخصی رد شد."
                    );


        } catch (
                IllegalArgumentException
                |
                IllegalStateException exception
        ) {


            redirectAttributes
                    .addFlashAttribute(

                            "errorMessage",

                            exception.getMessage()
                    );
        }


        return "redirect:/manager/leaves?status=PENDING";
    }


    /*
     * =====================================================
     * مأموریت‌های کارکنان واحد
     * =====================================================
     */

    @GetMapping("/missions")
    public String missions(

            @RequestParam(
                    defaultValue = "ALL"
            )
            String status,

            Authentication authentication,

            Model model

    ) {


        Employee manager =
                currentManagerService
                        .getCurrentManager(
                                authentication
                        );


        Long departmentId =
                manager
                        .getDepartment()
                        .getId();


        String selectedStatus =
                normalizeStatus(
                        status
                );


        List<MissionRequest> allRequests =
                missionRequestRepository
                        .findByEmployeeDepartmentIdOrderByCreatedAtDescIdDesc(
                                departmentId
                        );


        List<MissionRequest> filteredRequests;


        if (
                selectedStatus.equals(
                        "PENDING"
                )
        ) {


            filteredRequests =
                    missionRequestRepository
                            .findByEmployeeDepartmentIdAndStatusOrderByCreatedAtDescIdDesc(

                                    departmentId,

                                    MissionStatus.PENDING
                            );


        } else if (
                selectedStatus.equals(
                        "APPROVED"
                )
        ) {


            filteredRequests =
                    missionRequestRepository
                            .findByEmployeeDepartmentIdAndStatusOrderByCreatedAtDescIdDesc(

                                    departmentId,

                                    MissionStatus.APPROVED
                            );


        } else if (
                selectedStatus.equals(
                        "REJECTED"
                )
        ) {


            filteredRequests =
                    missionRequestRepository
                            .findByEmployeeDepartmentIdAndStatusOrderByCreatedAtDescIdDesc(

                                    departmentId,

                                    MissionStatus.REJECTED
                            );


        } else {


            filteredRequests =
                    allRequests;
        }


        long pendingCount =
                allRequests
                        .stream()
                        .filter(
                                MissionRequest::isPending
                        )
                        .count();


        long approvedCount =
                allRequests
                        .stream()
                        .filter(
                                MissionRequest::isApproved
                        )
                        .count();


        long rejectedCount =
                allRequests
                        .stream()
                        .filter(
                                MissionRequest::isRejected
                        )
                        .count();


        model.addAttribute(
                "manager",
                manager
        );


        model.addAttribute(
                "department",
                manager.getDepartment()
        );


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


        return "manager-missions";
    }


    /*
     * =====================================================
     * تأیید مأموریت
     * =====================================================
     */

    @PostMapping("/missions/{id}/approve")
    public String approveMission(

            @PathVariable Long id,

            @RequestParam(required = false)
            String reviewNote,

            Authentication authentication,

            RedirectAttributes redirectAttributes

    ) {


        try {


            Employee manager =
                    currentManagerService
                            .getCurrentManager(
                                    authentication
                            );


            MissionRequest request =
                    getManagerMissionRequest(
                            id,
                            manager
                    );


            ensureNotOwnRequest(

                    manager,

                    request
                            .getEmployee()
                            .getId()
            );


            if (!request.isPending()) {

                throw new IllegalStateException(
                        "فقط درخواست در انتظار بررسی قابل تأیید است."
                );
            }


            missionRequestService
                    .approveRequest(
                            request.getId(),
                            reviewNote
                    );


            redirectAttributes
                    .addFlashAttribute(

                            "successMessage",

                            "درخواست مأموریت با موفقیت تأیید شد."
                    );


        } catch (
                IllegalArgumentException
                |
                IllegalStateException exception
        ) {


            redirectAttributes
                    .addFlashAttribute(

                            "errorMessage",

                            exception.getMessage()
                    );
        }


        return "redirect:/manager/missions?status=PENDING";
    }


    /*
     * =====================================================
     * رد مأموریت
     * =====================================================
     */

    @PostMapping("/missions/{id}/reject")
    public String rejectMission(

            @PathVariable Long id,

            @RequestParam(required = false)
            String reviewNote,

            Authentication authentication,

            RedirectAttributes redirectAttributes

    ) {


        try {


            Employee manager =
                    currentManagerService
                            .getCurrentManager(
                                    authentication
                            );


            MissionRequest request =
                    getManagerMissionRequest(
                            id,
                            manager
                    );


            ensureNotOwnRequest(

                    manager,

                    request
                            .getEmployee()
                            .getId()
            );


            if (!request.isPending()) {

                throw new IllegalStateException(
                        "فقط درخواست در انتظار بررسی قابل رد است."
                );
            }


            missionRequestService
                    .rejectRequest(
                            request.getId(),
                            reviewNote
                    );


            redirectAttributes
                    .addFlashAttribute(

                            "successMessage",

                            "درخواست مأموریت رد شد."
                    );


        } catch (
                IllegalArgumentException
                |
                IllegalStateException exception
        ) {


            redirectAttributes
                    .addFlashAttribute(

                            "errorMessage",

                            exception.getMessage()
                    );
        }


        return "redirect:/manager/missions?status=PENDING";
    }


    /*
     * =====================================================
     * دریافت امن LeaveRequest
     *
     * ID + Department با هم بررسی می‌شوند.
     * =====================================================
     */

    private LeaveRequest getManagerLeaveRequest(

            Long id,

            Employee manager

    ) {


        Long departmentId =
                manager
                        .getDepartment()
                        .getId();


        return leaveRequestRepository
                .findByIdAndEmployeeDepartmentId(

                        id,

                        departmentId
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "درخواست مرخصی پیدا نشد یا متعلق به واحد شما نیست."
                        )
                );
    }


    /*
     * =====================================================
     * دریافت امن MissionRequest
     * =====================================================
     */

    private MissionRequest getManagerMissionRequest(

            Long id,

            Employee manager

    ) {


        Long departmentId =
                manager
                        .getDepartment()
                        .getId();


        return missionRequestRepository
                .findByIdAndEmployeeDepartmentId(

                        id,

                        departmentId
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "درخواست مأموریت پیدا نشد یا متعلق به واحد شما نیست."
                        )
                );
    }


    /*
     * =====================================================
     * جلوگیری از Self Approval
     * =====================================================
     */

    private void ensureNotOwnRequest(

            Employee manager,

            Long requestEmployeeId

    ) {


        if (
                requestEmployeeId != null
                        &&
                        requestEmployeeId.equals(
                                manager.getId()
                        )
        ) {


            throw new IllegalStateException(
                    "مدیر بخش نمی‌تواند درخواست شخصی خودش را تأیید یا رد کند؛ این درخواست باید توسط مدیر امور اداری بررسی شود."
            );
        }
    }


    /*
     * =====================================================
     * Status
     * =====================================================
     */

    private String normalizeStatus(
            String status
    ) {


        if (status == null) {

            return "ALL";
        }


        String normalized =
                status
                        .trim()
                        .toUpperCase();


        return switch (
                normalized
                ) {


            case "PENDING",
                 "APPROVED",
                 "REJECTED" ->

                    normalized;


            default ->

                    "ALL";
        };
    }

}