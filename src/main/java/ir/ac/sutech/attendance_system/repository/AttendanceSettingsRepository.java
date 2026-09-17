package ir.ac.sutech.attendance_system.repository;

import ir.ac.sutech.attendance_system.model.AttendanceSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceSettingsRepository
        extends JpaRepository<AttendanceSettings, Long> {
}
