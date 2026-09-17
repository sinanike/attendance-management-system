package ir.ac.sutech.attendance_system.service;

import ir.ac.sutech.attendance_system.dto.AttendanceSettingsForm;
import ir.ac.sutech.attendance_system.model.AttendanceSettings;
import ir.ac.sutech.attendance_system.repository.AttendanceSettingsRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AttendanceSettingsService {

    private final AttendanceSettingsRepository
            attendanceSettingsRepository;

    public AttendanceSettingsService(
            AttendanceSettingsRepository attendanceSettingsRepository
    ) {

        this.attendanceSettingsRepository =
                attendanceSettingsRepository;
    }

    @Transactional
    public AttendanceSettings getSettings() {

        return attendanceSettingsRepository
                .findById(
                        AttendanceSettings.SINGLETON_ID
                )
                .orElseGet(() ->
                        attendanceSettingsRepository
                                .save(
                                        new AttendanceSettings()
                                )
                );
    }

    @Transactional(readOnly = true)
    public AttendanceSettingsForm getForm() {

        AttendanceSettings settings =
                attendanceSettingsRepository
                        .findById(
                                AttendanceSettings.SINGLETON_ID
                        )
                        .orElse(null);

        AttendanceSettingsForm form =
                new AttendanceSettingsForm();

        if (settings == null) {
            return form;
        }

        form.setManualCorrectionEnabled(
                settings.isManualCorrectionEnabled()
        );

        form.setMaxCorrectionDays(
                settings.getMaxCorrectionDays()
        );

        form.setAllowManualRecordDeletion(
                settings.isAllowManualRecordDeletion()
        );

        form.setNote(
                settings.getNote()
        );

        return form;
    }

    @Transactional
    public AttendanceSettings update(
            AttendanceSettingsForm form
    ) {

        AttendanceSettings settings =
                getSettings();

        settings.setManualCorrectionEnabled(
                form.isManualCorrectionEnabled()
        );

        settings.setMaxCorrectionDays(
                form.getMaxCorrectionDays()
        );

        settings.setAllowManualRecordDeletion(
                form.isAllowManualRecordDeletion()
        );

        String note =
                form.getNote();

        settings.setNote(
                note == null
                        ? null
                        : note.trim()
        );

        return attendanceSettingsRepository
                .save(settings);
    }
}
