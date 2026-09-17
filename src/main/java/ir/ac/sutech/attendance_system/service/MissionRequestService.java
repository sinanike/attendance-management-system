package ir.ac.sutech.attendance_system.service;

import ir.ac.sutech.attendance_system.dto.MissionRequestForm;
import ir.ac.sutech.attendance_system.model.Employee;
import ir.ac.sutech.attendance_system.model.MissionRequest;
import ir.ac.sutech.attendance_system.model.MissionStatus;
import ir.ac.sutech.attendance_system.repository.EmployeeRepository;
import ir.ac.sutech.attendance_system.repository.MissionRequestRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class MissionRequestService {

    private final MissionRequestRepository
            missionRequestRepository;

    private final EmployeeRepository
            employeeRepository;


    public MissionRequestService(
            MissionRequestRepository
                    missionRequestRepository,

            EmployeeRepository
                    employeeRepository
    ) {

        this.missionRequestRepository =
                missionRequestRepository;

        this.employeeRepository =
                employeeRepository;
    }


    @Transactional(readOnly = true)
    public List<MissionRequest>
    findAllRequests() {

        return missionRequestRepository
                .findAllByOrderByCreatedAtDescIdDesc();
    }


    @Transactional
    public MissionRequest createRequest(
            MissionRequestForm form
    ) {

        Employee employee =
                employeeRepository
                        .findById(
                                form.getEmployeeId()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "کارمند انتخاب‌شده پیدا نشد."
                                )
                        );

        if (!employee.isActive()) {
            throw new IllegalArgumentException(
                    "کارمند انتخاب‌شده غیرفعال است."
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
                    "تاریخ پایان مأموریت نمی‌تواند قبل از تاریخ شروع باشد."
            );
        }


        List<MissionRequest> overlaps =
                missionRequestRepository
                        .findOverlappingActiveRequests(
                                employee.getId(),
                                startDate,
                                endDate,
                                MissionStatus.REJECTED
                        );

        if (!overlaps.isEmpty()) {

            throw new IllegalArgumentException(
                    "برای این کارمند در بازه انتخاب‌شده مأموریت دیگری ثبت شده است."
            );
        }


        MissionRequest request =
                new MissionRequest(
                        employee,
                        startDate,
                        endDate
                );

        request.setDestination(
                normalize(
                        form.getDestination()
                )
        );

        request.setPurpose(
                normalize(
                        form.getPurpose()
                )
        );


        return missionRequestRepository
                .save(request);
    }


    @Transactional
    public void approveRequest(
            Long id,
            String note
    ) {

        MissionRequest request =
                getRequired(id);

        request.approve(
                normalize(note)
        );
    }


    @Transactional
    public void rejectRequest(
            Long id,
            String note
    ) {

        MissionRequest request =
                getRequired(id);

        request.reject(
                normalize(note)
        );
    }


    @Transactional
    public void markPending(
            Long id
    ) {

        MissionRequest request =
                getRequired(id);

        request.markPending();
    }


    private MissionRequest getRequired(
            Long id
    ) {

        return missionRequestRepository
                .findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "درخواست مأموریت پیدا نشد."
                        )
                );
    }


    private String normalize(
            String value
    ) {

        if (
                value == null
                        || value.isBlank()
        ) {
            return null;
        }

        return value.trim();
    }
}