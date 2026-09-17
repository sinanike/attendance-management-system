package ir.ac.sutech.attendance_system.service;

import ir.ac.sutech.attendance_system.dto.AttendanceRecordForm;
import ir.ac.sutech.attendance_system.model.AttendanceRecord;
import ir.ac.sutech.attendance_system.model.AttendanceSettings;
import ir.ac.sutech.attendance_system.model.AttendanceSource;
import ir.ac.sutech.attendance_system.model.Employee;
import ir.ac.sutech.attendance_system.repository.AttendanceRecordRepository;
import ir.ac.sutech.attendance_system.repository.EmployeeRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class OperatorAttendanceService {

    private final AttendanceRecordRepository
            attendanceRecordRepository;

    private final EmployeeRepository
            employeeRepository;

    private final AttendanceSettingsService
            attendanceSettingsService;

    public OperatorAttendanceService(
            AttendanceRecordRepository attendanceRecordRepository,
            EmployeeRepository employeeRepository,
            AttendanceSettingsService attendanceSettingsService
    ) {

        this.attendanceRecordRepository =
                attendanceRecordRepository;

        this.employeeRepository =
                employeeRepository;

        this.attendanceSettingsService =
                attendanceSettingsService;
    }

    public List<AttendanceRecord> findAllRecords() {

        return attendanceRecordRepository
                .findAllByOrderByEventTimeDesc();
    }

    public List<Employee> findActiveEmployees() {

        return employeeRepository
                .findByActiveTrueOrderByLastNameAscFirstNameAsc();
    }

    public AttendanceRecordForm findFormByRecordId(
            Long recordId
    ) {

        AttendanceRecord record =
                findRecordOrThrow(recordId);

        ensureManualRecord(record);

        AttendanceRecordForm form =
                new AttendanceRecordForm();

        form.setEmployeeId(
                record.getEmployee().getId()
        );

        form.setEventTime(
                record.getEventTime()
        );

        form.setAttendanceType(
                record.getAttendanceType()
        );

        return form;
    }

    @Transactional
    public AttendanceRecord createCorrection(
            AttendanceRecordForm form
    ) {

        validateCorrectionPolicy(
                form
        );

        Employee employee =
                findActiveEmployee(
                        form.getEmployeeId()
                );

        AttendanceRecord record =
                new AttendanceRecord();

        record.setEmployee(employee);

        record.setEventTime(
                form.getEventTime()
        );

        record.setAttendanceType(
                form.getAttendanceType()
        );

        record.setAttendanceSource(
                AttendanceSource.MANUAL
        );

        return attendanceRecordRepository
                .save(record);
    }

    @Transactional
    public AttendanceRecord updateCorrection(
            Long recordId,
            AttendanceRecordForm form
    ) {

        AttendanceRecord record =
                findRecordOrThrow(recordId);

        ensureManualRecord(record);

        validateCorrectionPolicy(
                form
        );

        Employee employee =
                findActiveEmployee(
                        form.getEmployeeId()
                );

        record.setEmployee(employee);

        record.setEventTime(
                form.getEventTime()
        );

        record.setAttendanceType(
                form.getAttendanceType()
        );

        return attendanceRecordRepository
                .save(record);
    }

    @Transactional
    public void deleteCorrection(
            Long recordId
    ) {

        AttendanceSettings settings =
                attendanceSettingsService
                        .getSettings();

        if (
                !settings
                        .isAllowManualRecordDeletion()
        ) {
            throw new IllegalArgumentException(
                    "حذف اصلاحات دستی در تنظیمات سامانه غیرفعال است."
            );
        }

        AttendanceRecord record =
                findRecordOrThrow(recordId);

        ensureManualRecord(record);

        attendanceRecordRepository
                .delete(record);
    }

    private void validateCorrectionPolicy(
            AttendanceRecordForm form
    ) {

        AttendanceSettings settings =
                attendanceSettingsService
                        .getSettings();

        if (
                !settings
                        .isManualCorrectionEnabled()
        ) {
            throw new IllegalArgumentException(
                    "اصلاح دستی تردد در تنظیمات سامانه غیرفعال است."
            );
        }

        if (form.getEmployeeId() == null) {
            throw new IllegalArgumentException(
                    "کارمند را انتخاب کنید."
            );
        }

        if (form.getAttendanceType() == null) {
            throw new IllegalArgumentException(
                    "نوع تردد را انتخاب کنید."
            );
        }

        LocalDateTime eventTime =
                form.getEventTime();

        if (eventTime == null) {
            throw new IllegalArgumentException(
                    "تاریخ و ساعت تردد را وارد کنید."
            );
        }

        if (
                eventTime.isAfter(
                        LocalDateTime.now()
                                .plusMinutes(5)
                )
        ) {
            throw new IllegalArgumentException(
                    "زمان تردد نمی‌تواند در آینده باشد."
            );
        }

        int maxDays =
                Math.max(
                        settings.getMaxCorrectionDays(),
                        0
                );

        LocalDate oldestAllowedDate =
                LocalDate.now()
                        .minusDays(maxDays);

        if (
                eventTime
                        .toLocalDate()
                        .isBefore(
                                oldestAllowedDate
                        )
        ) {
            throw new IllegalArgumentException(
                    "این تردد خارج از بازه مجاز اصلاح است. حداکثر بازه فعلی "
                            + maxDays
                            + " روز است."
            );
        }
    }

    private Employee findActiveEmployee(
            Long employeeId
    ) {

        return employeeRepository
                .findById(employeeId)
                .filter(Employee::isActive)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "کارمند انتخاب‌شده پیدا نشد یا غیرفعال است."
                        )
                );
    }

    private AttendanceRecord findRecordOrThrow(
            Long recordId
    ) {

        return attendanceRecordRepository
                .findById(recordId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "رکورد تردد پیدا نشد."
                        )
                );
    }

    private void ensureManualRecord(
            AttendanceRecord record
    ) {

        if (
                record.getAttendanceSource()
                        != AttendanceSource.MANUAL
        ) {
            throw new IllegalArgumentException(
                    "رکورد خام دستگاه مستقیماً ویرایش یا حذف نمی‌شود. برای اصلاح، یک ثبت دستی جبرانی ایجاد کنید."
            );
        }
    }
}
