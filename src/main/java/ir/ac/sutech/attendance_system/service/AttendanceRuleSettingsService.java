package ir.ac.sutech.attendance_system.service;

import ir.ac.sutech.attendance_system.dto.AttendanceRuleSettingsForm;
import ir.ac.sutech.attendance_system.model.AttendanceRuleSettings;
import ir.ac.sutech.attendance_system.repository.AttendanceRuleSettingsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AttendanceRuleSettingsService {

    private final AttendanceRuleSettingsRepository repository;

    public AttendanceRuleSettingsService(
            AttendanceRuleSettingsRepository repository
    ) {
        this.repository = repository;
    }

    public AttendanceRuleSettings getSettings() {
        return repository.findById(AttendanceRuleSettings.SINGLETON_ID)
                .orElseGet(AttendanceRuleSettings::defaultSettings);
    }

    public AttendanceRuleSettingsForm getForm() {
        AttendanceRuleSettings settings = getSettings();

        AttendanceRuleSettingsForm form =
                new AttendanceRuleSettingsForm();

        form.setAnnualLeaveDays(
                settings.getAnnualLeaveDays()
        );

        form.setMinimumAttendancePercent(
                settings.getMinimumAttendancePercent()
        );

        return form;
    }

    @Transactional
    public AttendanceRuleSettings save(
            AttendanceRuleSettingsForm form
    ) {
        if (form.getAnnualLeaveDays() < 0
                || form.getAnnualLeaveDays() > 365) {
            throw new IllegalArgumentException(
                    "تعداد روز مرخصی سالانه باید بین صفر تا ۳۶۵ باشد."
            );
        }

        if (form.getMinimumAttendancePercent() < 0
                || form.getMinimumAttendancePercent() > 100) {
            throw new IllegalArgumentException(
                    "حد نصاب حضور باید بین صفر تا ۱۰۰ درصد باشد."
            );
        }

        AttendanceRuleSettings settings =
                repository.findById(
                                AttendanceRuleSettings.SINGLETON_ID
                        )
                        .orElseGet(
                                AttendanceRuleSettings::defaultSettings
                        );

        settings.setAnnualLeaveDays(
                form.getAnnualLeaveDays()
        );

        settings.setMinimumAttendancePercent(
                form.getMinimumAttendancePercent()
        );

        return repository.save(settings);
    }
}
