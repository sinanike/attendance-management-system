package ir.ac.sutech.attendance_system.service;

import ir.ac.sutech.attendance_system.dto.ShiftDayScheduleRow;
import ir.ac.sutech.attendance_system.dto.ShiftWeeklyScheduleForm;
import ir.ac.sutech.attendance_system.model.Shift;
import ir.ac.sutech.attendance_system.model.ShiftDaySchedule;
import ir.ac.sutech.attendance_system.model.WorkDay;
import ir.ac.sutech.attendance_system.repository.ShiftDayScheduleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class ShiftWeeklyScheduleService {

    private final ShiftDayScheduleRepository scheduleRepository;
    private final ShiftService shiftService;

    public ShiftWeeklyScheduleService(
            ShiftDayScheduleRepository scheduleRepository,
            ShiftService shiftService
    ) {
        this.scheduleRepository = scheduleRepository;
        this.shiftService = shiftService;
    }

    public ShiftWeeklyScheduleForm getForm(Long shiftId) {
        Shift shift = shiftService.findShiftById(shiftId);

        Map<WorkDay, ShiftDaySchedule> existing =
                new EnumMap<>(WorkDay.class);

        scheduleRepository.findByShiftId(shiftId)
                .forEach(item ->
                        existing.put(item.getWorkDay(), item));

        ShiftWeeklyScheduleForm form =
                new ShiftWeeklyScheduleForm();

        for (WorkDay workDay : WorkDay.values()) {
            ShiftDayScheduleRow row =
                    new ShiftDayScheduleRow();

            row.setWorkDay(workDay);

            ShiftDaySchedule saved = existing.get(workDay);

            if (saved != null) {
                copy(saved, row);
            } else {
                row.setWorkingDay(
                        shift.getWorkDays().contains(workDay)
                );

                row.setStartTime(shift.getStartTime());
                row.setEndTime(shift.getEndTime());

                row.setLateGraceMinutes(
                        shift.getLateGraceMinutes()
                );

                row.setEarlyLeaveGraceMinutes(
                        shift.getEarlyLeaveGraceMinutes()
                );
            }

            form.getDays().add(row);
        }

        return form;
    }

    @Transactional
    public void save(
            Long shiftId,
            ShiftWeeklyScheduleForm form
    ) {
        Shift shift =
                shiftService.findShiftById(shiftId);

        validate(form);

        Map<WorkDay, ShiftDaySchedule> existing =
                new EnumMap<>(WorkDay.class);

        scheduleRepository.findByShiftId(shiftId)
                .forEach(item ->
                        existing.put(item.getWorkDay(), item));

        EnumSet<WorkDay> activeDays =
                EnumSet.noneOf(WorkDay.class);

        for (ShiftDayScheduleRow row : form.getDays()) {
            ShiftDaySchedule schedule =
                    existing.get(row.getWorkDay());

            if (schedule == null) {
                schedule = new ShiftDaySchedule(
                        shift,
                        row.getWorkDay()
                );
            }

            schedule.setWorkingDay(row.isWorkingDay());

            schedule.setStartTime(
                    row.isWorkingDay()
                            ? row.getStartTime()
                            : null
            );

            schedule.setEndTime(
                    row.isWorkingDay()
                            ? row.getEndTime()
                            : null
            );

            schedule.setBreakMinutes(
                    value(row.getBreakMinutes())
            );

            schedule.setLateGraceMinutes(
                    value(row.getLateGraceMinutes())
            );

            schedule.setEarlyLeaveGraceMinutes(
                    value(row.getEarlyLeaveGraceMinutes())
            );

            scheduleRepository.save(schedule);

            if (row.isWorkingDay()) {
                activeDays.add(row.getWorkDay());
            }
        }

        /*
         * برای حفظ سازگاری با گزارش‌های فعلی
         * که هنوز از Shift.workDays استفاده می‌کنند.
         */
        shift.setWorkDays(activeDays);
    }

    public List<ShiftDaySchedule> findByShiftId(
            Long shiftId
    ) {
        return scheduleRepository.findByShiftId(shiftId)
                .stream()
                .sorted((first, second) ->
                        Integer.compare(
                                first.getWorkDay().ordinal(),
                                second.getWorkDay().ordinal()
                        )
                )
                .toList();
    }

    private void validate(
            ShiftWeeklyScheduleForm form
    ) {
        if (form.getDays() == null
                || form.getDays().size()
                != WorkDay.values().length) {

            throw new IllegalArgumentException(
                    "برنامه هفتگی ناقص است."
            );
        }

        boolean hasWorkingDay = false;

        EnumSet<WorkDay> receivedDays =
                EnumSet.noneOf(WorkDay.class);

        for (ShiftDayScheduleRow row : form.getDays()) {
            if (row.getWorkDay() == null) {
                throw new IllegalArgumentException(
                        "روز هفته نامعتبر است."
                );
            }

            if (!receivedDays.add(row.getWorkDay())) {
                throw new IllegalArgumentException(
                        "یک روز هفته بیش از یک‌بار ارسال شده است."
                );
            }

            if (!row.isWorkingDay()) {
                continue;
            }

            hasWorkingDay = true;

            if (row.getStartTime() == null
                    || row.getEndTime() == null) {

                throw new IllegalArgumentException(
                        "برای "
                                + row.getWorkDay().getPersianTitle()
                                + " ساعت شروع و پایان را وارد کنید."
                );
            }

            if (row.getStartTime().equals(row.getEndTime())) {
                throw new IllegalArgumentException(
                        "ساعت شروع و پایان "
                                + row.getWorkDay().getPersianTitle()
                                + " نمی‌تواند یکسان باشد."
                );
            }

            validateMinutes(
                    row.getBreakMinutes(),
                    480,
                    "زمان استراحت",
                    row.getWorkDay()
            );

            validateMinutes(
                    row.getLateGraceMinutes(),
                    180,
                    "تأخیر مجاز",
                    row.getWorkDay()
            );

            validateMinutes(
                    row.getEarlyLeaveGraceMinutes(),
                    180,
                    "خروج زودتر مجاز",
                    row.getWorkDay()
            );
        }

        if (!hasWorkingDay) {
            throw new IllegalArgumentException(
                    "حداقل یک روز کاری را فعال کنید."
            );
        }
    }

    private void validateMinutes(
            Integer amount,
            int max,
            String title,
            WorkDay day
    ) {
        if (amount == null
                || amount < 0
                || amount > max) {

            throw new IllegalArgumentException(
                    title
                            + " در "
                            + day.getPersianTitle()
                            + " باید بین صفر تا "
                            + max
                            + " دقیقه باشد."
            );
        }
    }

    private int value(Integer value) {
        return value == null ? 0 : value;
    }

    private void copy(
            ShiftDaySchedule source,
            ShiftDayScheduleRow target
    ) {
        target.setWorkingDay(source.isWorkingDay());
        target.setStartTime(source.getStartTime());
        target.setEndTime(source.getEndTime());

        target.setBreakMinutes(
                source.getBreakMinutes()
        );

        target.setLateGraceMinutes(
                source.getLateGraceMinutes()
        );

        target.setEarlyLeaveGraceMinutes(
                source.getEarlyLeaveGraceMinutes()
        );
    }
}