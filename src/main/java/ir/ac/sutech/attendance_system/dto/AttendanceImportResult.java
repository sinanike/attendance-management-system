package ir.ac.sutech.attendance_system.dto;

import java.util.List;

public record AttendanceImportResult(
        int importedCount,
        int duplicateCount,
        int rejectedCount,
        List<String> errors
) {

    public String getSummary() {
        return importedCount + " تردد وارد شد، "
                + duplicateCount + " تکراری و "
                + rejectedCount + " نامعتبر بود.";
    }
}