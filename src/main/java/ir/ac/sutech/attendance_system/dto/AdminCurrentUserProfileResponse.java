package ir.ac.sutech.attendance_system.dto;

public record AdminCurrentUserProfileResponse(

        String username,

        String role,

        String roleDisplayName,

        boolean active,

        boolean employeeLinked,

        Long employeeId,

        String fullName,

        String personnelCode,

        String nationalCode,

        String department

) {
}
