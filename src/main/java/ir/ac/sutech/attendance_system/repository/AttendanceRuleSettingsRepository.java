package ir.ac.sutech.attendance_system.repository;

import ir.ac.sutech.attendance_system.model.AttendanceRuleSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceRuleSettingsRepository
        extends JpaRepository<AttendanceRuleSettings, Long> {
}
