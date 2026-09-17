package ir.ac.sutech.attendance_system.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AdminDashboardOverviewResponse(

        long activeEmployeeCount,

        long presentCount,

        long absentCount,

        long leaveCount,

        long missionCount,

        long lateCount,

        long incompleteCount,

        double attendanceRate,

        LocalDate date,

        LocalDateTime generatedAt

) {
}
