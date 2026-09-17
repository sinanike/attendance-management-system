package ir.ac.sutech.attendance_system.controller;

import ir.ac.sutech.attendance_system.dto.AdminDashboardAiResponse;
import ir.ac.sutech.attendance_system.dto.AdminDashboardDataResponse;
import ir.ac.sutech.attendance_system.dto.AdminDashboardOverviewResponse;

import ir.ac.sutech.attendance_system.service.AdminDashboardApiService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/api/dashboard")
public class AdminDashboardApiController {

    private final AdminDashboardApiService
            adminDashboardApiService;


    public AdminDashboardApiController(

            AdminDashboardApiService adminDashboardApiService

    ) {

        this.adminDashboardApiService =
                adminDashboardApiService;
    }


    /*
     * سازگاری با مرحله قبل
     */

    @GetMapping("/overview")
    public AdminDashboardOverviewResponse
    getOverview() {

        return adminDashboardApiService
                .getOverview();
    }


    /*
     * همه داده‌های زنده داشبورد
     */

    @GetMapping("/data")
    public AdminDashboardDataResponse
    getDashboardData() {

        return adminDashboardApiService
                .getDashboardData();
    }


    /*
     * تحلیل Gemini
     *
     * عمداً Endpoint جدا دارد
     * تا هر 10 ثانیه Gemini فراخوانی نشود.
     */

    @GetMapping("/ai")
    public AdminDashboardAiResponse
    getAiAnalysis() {

        return adminDashboardApiService
                .getAiAnalysis();
    }
}
