package ir.ac.sutech.attendance_system.repository;

import ir.ac.sutech.attendance_system.model.OfficialHoliday;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface OfficialHolidayRepository
        extends JpaRepository<
        OfficialHoliday,
        Long
        > {

    Optional<OfficialHoliday>
    findByHolidayDateAndActiveTrue(
            LocalDate holidayDate
    );


    Optional<OfficialHoliday>
    findByHolidayDate(
            LocalDate holidayDate
    );


    boolean existsByHolidayDate(
            LocalDate holidayDate
    );


    List<OfficialHoliday>
    findAllByOrderByHolidayDateDesc();
}