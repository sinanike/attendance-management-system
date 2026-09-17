package ir.ac.sutech.attendance_system.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record AdminDashboardDataResponse(

        AdminDashboardOverviewResponse overview,

        List<TrendPoint> trend,

        List<StatusPoint> status,

        List<DepartmentPoint> departments,

        List<RecentAttendanceItem> recentAttendance,

        List<PendingRequestItem> pendingRequests,

        List<AlertItem> alerts,

        LocalDateTime generatedAt

) {

    public record TrendPoint(
            LocalDate date,
            double rate
    ) {
    }


    public record StatusPoint(
            String name,
            long value
    ) {
    }


    public record DepartmentPoint(
            Long departmentId,
            String name,
            double rate
    ) {
    }


    public record RecentAttendanceItem(
            Long id,
            String name,
            String department,
            String type,
            LocalDate date,
            String time,
            String state
    ) {
    }


    public record PendingRequestItem(
            String key,
            String type,
            String name,
            String department,
            LocalDate date,
            String status,
            LocalDateTime createdAt
    ) {
    }


    public record AlertItem(
            String id,
            String tone,
            String text
    ) {
    }
}
