package ir.ac.sutech.attendance_system.service;

import ir.ac.sutech.attendance_system.dto.AttendanceRecordForm;
import ir.ac.sutech.attendance_system.model.AttendanceRecord;
import ir.ac.sutech.attendance_system.model.AttendanceSource;
import ir.ac.sutech.attendance_system.model.Employee;
import ir.ac.sutech.attendance_system.repository.AttendanceRecordRepository;
import ir.ac.sutech.attendance_system.repository.EmployeeRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class AttendanceRecordService {

    private final AttendanceRecordRepository attendanceRecordRepository;
    private final EmployeeRepository employeeRepository;

    public AttendanceRecordService(
            AttendanceRecordRepository attendanceRecordRepository,
            EmployeeRepository employeeRepository
    ) {
        this.attendanceRecordRepository =
                attendanceRecordRepository;

        this.employeeRepository =
                employeeRepository;
    }

    public List<AttendanceRecord> findAllRecords() {
        return attendanceRecordRepository
                .findAllByOrderByEventTimeDesc();
    }

    public List<Employee> findActiveEmployees() {
        return employeeRepository
                .findByActiveTrueOrderByLastNameAscFirstNameAsc();
    }

    @Transactional
    public AttendanceRecord createManualRecord(
            AttendanceRecordForm form
    ) {
        validateManualRecordForm(form);

        Employee employee = employeeRepository
                .findById(form.getEmployeeId())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "کارمند انتخاب‌شده پیدا نشد."
                        )
                );

        if (!employee.isActive()) {
            throw new IllegalArgumentException(
                    "امکان ثبت تردد برای کارمند غیرفعال وجود ندارد."
            );
        }

        AttendanceRecord record =
                new AttendanceRecord();

        record.setEmployee(employee);
        record.setEventTime(form.getEventTime());
        record.setAttendanceType(
                form.getAttendanceType()
        );
        record.setAttendanceSource(
                AttendanceSource.MANUAL
        );

        return attendanceRecordRepository.save(record);
    }
    @Transactional
    public void deleteRecord(Long recordId) {

        if (recordId == null) {
            throw new IllegalArgumentException(
                    "شناسه تردد معتبر نیست."
            );
        }

        AttendanceRecord record = attendanceRecordRepository
                .findById(recordId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "تردد موردنظر پیدا نشد."
                        )
                );

        attendanceRecordRepository.delete(record);
    }
    public List<AttendanceRecord> findRecordsForReport(
            LocalDate fromDate,
            LocalDate toDate,
            Long employeeId
    ) {
        if (fromDate == null || toDate == null) {
            throw new IllegalArgumentException(
                    "تاریخ شروع و پایان گزارش الزامی است."
            );
        }

        if (toDate.isBefore(fromDate)) {
            throw new IllegalArgumentException(
                    "تاریخ پایان نمی‌تواند قبل از تاریخ شروع باشد."
            );
        }

        LocalDateTime startTime =
                fromDate.atStartOfDay();

        LocalDateTime endTime =
                toDate.plusDays(1).atStartOfDay();

        if (employeeId == null) {
            return attendanceRecordRepository
                    .findByEventTimeGreaterThanEqualAndEventTimeLessThanOrderByEventTimeDesc(
                            startTime,
                            endTime
                    );
        }

        return attendanceRecordRepository
                .findByEmployeeIdAndEventTimeGreaterThanEqualAndEventTimeLessThanOrderByEventTimeDesc(
                        employeeId,
                        startTime,
                        endTime
                );
    }

    private void validateManualRecordForm(
            AttendanceRecordForm form
    ) {
        if (form == null) {
            throw new IllegalArgumentException(
                    "اطلاعات تردد ارسال نشده است."
            );
        }

        if (form.getEmployeeId() == null) {
            throw new IllegalArgumentException(
                    "انتخاب کارمند الزامی است."
            );
        }

        if (form.getEventTime() == null) {
            throw new IllegalArgumentException(
                    "تاریخ و ساعت تردد الزامی است."
            );
        }

        if (form.getAttendanceType() == null) {
            throw new IllegalArgumentException(
                    "نوع تردد را انتخاب کنید."
            );
        }
    }
}