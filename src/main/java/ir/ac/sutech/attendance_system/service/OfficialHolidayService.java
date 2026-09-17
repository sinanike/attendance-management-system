package ir.ac.sutech.attendance_system.service;

import ir.ac.sutech.attendance_system.model.OfficialHoliday;
import ir.ac.sutech.attendance_system.repository.OfficialHolidayRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class OfficialHolidayService {

    private final OfficialHolidayRepository
            holidayRepository;


    public OfficialHolidayService(
            OfficialHolidayRepository
                    holidayRepository
    ) {
        this.holidayRepository =
                holidayRepository;
    }


    @Transactional(readOnly = true)
    public List<OfficialHoliday>
    findAll() {

        return holidayRepository
                .findAllByOrderByHolidayDateDesc();
    }


    @Transactional
    public OfficialHoliday create(
            LocalDate date,
            String title,
            String description
    ) {

        if (date == null) {
            throw new IllegalArgumentException(
                    "تاریخ تعطیلی الزامی است."
            );
        }

        if (
                title == null
                        || title.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "عنوان تعطیلی الزامی است."
            );
        }


        if (
                holidayRepository
                        .existsByHolidayDate(date)
        ) {

            throw new IllegalArgumentException(
                    "برای این تاریخ قبلاً تعطیلی ثبت شده است."
            );
        }


        OfficialHoliday holiday =
                new OfficialHoliday(
                        date,
                        title.trim()
                );

        if (
                description != null
                        && !description.isBlank()
        ) {
            holiday.setDescription(
                    description.trim()
            );
        }


        return holidayRepository
                .save(holiday);
    }


    @Transactional
    public void toggle(
            Long id
    ) {

        OfficialHoliday holiday =
                holidayRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "تعطیلی موردنظر پیدا نشد."
                                )
                        );

        holiday.setActive(
                !holiday.isActive()
        );
    }


    @Transactional(readOnly = true)
    public boolean isOfficialHoliday(
            LocalDate date
    ) {

        return holidayRepository
                .findByHolidayDateAndActiveTrue(
                        date
                )
                .isPresent();
    }
}