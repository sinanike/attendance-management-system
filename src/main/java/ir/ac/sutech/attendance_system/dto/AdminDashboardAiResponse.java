package ir.ac.sutech.attendance_system.dto;

import java.time.LocalDateTime;

public record AdminDashboardAiResponse(

        boolean available,

        String analysis,

        LocalDateTime generatedAt

) {
}
