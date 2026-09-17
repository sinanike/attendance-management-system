package ir.ac.sutech.attendance_system.service;

import ir.ac.sutech.attendance_system.dto.LeaveRequestForm;
import ir.ac.sutech.attendance_system.model.Employee;
import ir.ac.sutech.attendance_system.model.LeaveRequest;
import ir.ac.sutech.attendance_system.model.LeaveType;
import ir.ac.sutech.attendance_system.repository.EmployeeRepository;
import ir.ac.sutech.attendance_system.repository.LeaveRequestRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class LeaveRequestService {

    private final LeaveRequestRepository
            leaveRequestRepository;

    private final EmployeeRepository
            employeeRepository;

    public LeaveRequestService(
            LeaveRequestRepository leaveRequestRepository,
            EmployeeRepository employeeRepository
    ) {
        this.leaveRequestRepository =
                leaveRequestRepository;

        this.employeeRepository =
                employeeRepository;
    }

    /*
     * تمام درخواست‌های مرخصی
     */
    public List<LeaveRequest> findAllRequests() {

        return leaveRequestRepository
                .findAllByOrderByCreatedAtDescIdDesc();
    }

    /*
     * درخواست‌های یک کارمند
     */
    public List<LeaveRequest> findByEmployeeId(
            Long employeeId
    ) {

        return leaveRequestRepository
                .findByEmployeeIdOrderByCreatedAtDescIdDesc(
                        employeeId
                );
    }

    public long countRequests() {
        return leaveRequestRepository.count();
    }

    /*
     * ثبت درخواست مرخصی
     */
    @Transactional
    public LeaveRequest createRequest(
            LeaveRequestForm form
    ) {

        if (form == null) {
            throw new IllegalArgumentException(
                    "اطلاعات درخواست مرخصی نامعتبر است."
            );
        }

        if (form.getEmployeeId() == null) {
            throw new IllegalArgumentException(
                    "انتخاب کارمند الزامی است."
            );
        }

        if (form.getLeaveType() == null) {
            throw new IllegalArgumentException(
                    "نوع مرخصی الزامی است."
            );
        }

        if (form.getStartDate() == null) {
            throw new IllegalArgumentException(
                    "تاریخ شروع مرخصی الزامی است."
            );
        }

        Employee employee =
                employeeRepository
                        .findById(
                                form.getEmployeeId()
                        )
                        .orElseThrow(
                                () ->
                                        new EntityNotFoundException(
                                                "کارمند موردنظر پیدا نشد."
                                        )
                        );

        if (!employee.isActive()) {
            throw new IllegalStateException(
                    "برای کارمند غیرفعال نمی‌توان مرخصی ثبت کرد."
            );
        }

        LocalDate startDate =
                form.getStartDate();

        LocalDate endDate =
                form.getEndDate() == null
                        ? startDate
                        : form.getEndDate();

        if (endDate.isBefore(startDate)) {

            throw new IllegalArgumentException(
                    "تاریخ پایان نمی‌تواند قبل از تاریخ شروع باشد."
            );
        }

        /*
         * اعتبارسنجی مرخصی ساعتی
         */
        if (
                form.getLeaveType()
                        == LeaveType.HOURLY
        ) {

            validateHourlyLeave(
                    startDate,
                    endDate,
                    form.getStartTime(),
                    form.getEndTime()
            );

            /*
             * مرخصی ساعتی فقط مربوط
             * به یک روز است.
             */
            endDate = startDate;
        }

        /*
         * فعلاً اجازه نمی‌دهیم دو درخواست
         * تأییدشده یا در انتظار، روی یک بازه
         * برای یک کارمند هم‌پوشانی داشته باشند.
         */
        List<LeaveRequest> overlappingRequests =
                leaveRequestRepository
                        .findOverlappingActiveRequests(
                                employee.getId(),
                                startDate,
                                endDate
                        );

        if (!overlappingRequests.isEmpty()) {

            throw new IllegalArgumentException(
                    "برای این کارمند در تمام یا بخشی از بازه انتخاب‌شده، درخواست مرخصی دیگری وجود دارد."
            );
        }

        LeaveRequest request =
                new LeaveRequest(
                        employee,
                        form.getLeaveType(),
                        startDate,
                        endDate
                );

        /*
         * ساعت‌ها فقط برای مرخصی ساعتی
         * ذخیره می‌شوند.
         */
        if (
                form.getLeaveType()
                        == LeaveType.HOURLY
        ) {

            request.setStartTime(
                    form.getStartTime()
            );

            request.setEndTime(
                    form.getEndTime()
            );

        } else {

            request.setStartTime(null);
            request.setEndTime(null);
        }

        request.setReason(
                form.getReason()
        );

        return leaveRequestRepository.save(
                request
        );
    }

    /*
     * تأیید درخواست توسط مدیر
     */
    @Transactional
    public void approveRequest(
            Long requestId,
            String reviewNote
    ) {

        LeaveRequest request =
                findById(requestId);

        if (request.isApproved()) {

            throw new IllegalStateException(
                    "این درخواست قبلاً تأیید شده است."
            );
        }

        request.approve(
                reviewNote
        );
    }

    /*
     * رد درخواست
     */
    @Transactional
    public void rejectRequest(
            Long requestId,
            String reviewNote
    ) {

        LeaveRequest request =
                findById(requestId);

        if (request.isRejected()) {

            throw new IllegalStateException(
                    "این درخواست قبلاً رد شده است."
            );
        }

        request.reject(
                reviewNote
        );
    }

    /*
     * برگرداندن به حالت در انتظار بررسی
     */
    @Transactional
    public void markPending(
            Long requestId
    ) {

        LeaveRequest request =
                findById(requestId);

        request.markPending();
    }

    public LeaveRequest findById(
            Long requestId
    ) {

        return leaveRequestRepository
                .findById(requestId)
                .orElseThrow(
                        () ->
                                new EntityNotFoundException(
                                        "درخواست مرخصی موردنظر پیدا نشد."
                                )
                );
    }

    private void validateHourlyLeave(
            LocalDate startDate,
            LocalDate endDate,
            LocalTime startTime,
            LocalTime endTime
    ) {

        /*
         * مرخصی ساعتی باید در یک روز باشد.
         */
        if (
                endDate != null
                        && !startDate.equals(endDate)
        ) {

            throw new IllegalArgumentException(
                    "مرخصی ساعتی باید در یک روز ثبت شود."
            );
        }

        if (
                startTime == null
                        || endTime == null
        ) {

            throw new IllegalArgumentException(
                    "برای مرخصی ساعتی، ساعت شروع و پایان الزامی است."
            );
        }

        if (
                !endTime.isAfter(startTime)
        ) {

            throw new IllegalArgumentException(
                    "ساعت پایان مرخصی باید بعد از ساعت شروع باشد."
            );
        }
    }
}