package ir.ac.sutech.attendance_system.util;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Component("jalaliDateUtil")
public class JalaliDateUtil {

    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm");

    public String format(LocalDate gregorianDate) {
        if (gregorianDate == null) {
            return "";
        }

        int[] jalali = gregorianToJalali(
                gregorianDate.getYear(),
                gregorianDate.getMonthValue(),
                gregorianDate.getDayOfMonth()
        );

        return String.format(
                "%04d/%02d/%02d",
                jalali[0],
                jalali[1],
                jalali[2]
        );
    }

    public String formatDateTime(LocalDateTime gregorianDateTime) {
        if (gregorianDateTime == null) {
            return "";
        }

        return format(gregorianDateTime.toLocalDate())
                + " - "
                + gregorianDateTime.toLocalTime().format(TIME_FORMATTER);
    }

    public String formatDateTimeCompact(LocalDateTime gregorianDateTime) {
        if (gregorianDateTime == null) {
            return "";
        }

        return format(gregorianDateTime.toLocalDate())
                + " "
                + gregorianDateTime.toLocalTime().format(TIME_FORMATTER);
    }

    public String formatTime(LocalTime time) {
        if (time == null) {
            return "";
        }
        return time.format(TIME_FORMATTER);
    }

    public LocalDate parse(String jalaliDate) {
        if (jalaliDate == null || jalaliDate.isBlank()) {
            throw new IllegalArgumentException("تاریخ شمسی نمی‌تواند خالی باشد.");
        }

        String normalized = normalizeDigits(jalaliDate.trim())
                .replace('-', '/');

        String[] parts = normalized.split("/");

        if (parts.length != 3) {
            throw new IllegalArgumentException(
                    "فرمت تاریخ شمسی باید مانند 1405/05/19 باشد."
            );
        }

        try {
            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            int day = Integer.parseInt(parts[2]);

            return toGregorian(year, month, day);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "تاریخ شمسی باید فقط شامل عدد باشد."
            );
        }
    }

    public LocalDateTime parseDateTime(String jalaliDateTime) {
        if (jalaliDateTime == null || jalaliDateTime.isBlank()) {
            throw new IllegalArgumentException("تاریخ و ساعت شمسی نمی‌تواند خالی باشد.");
        }

        String normalized = normalizeDigits(jalaliDateTime.trim())
                .replace('T', ' ');

        String[] parts = normalized.split("\\s+");

        if (parts.length != 2) {
            throw new IllegalArgumentException(
                    "فرمت تاریخ و ساعت باید مانند 1405/05/19 08:30 باشد."
            );
        }

        LocalDate date = parse(parts[0]);
        LocalTime time;

        try {
            time = LocalTime.parse(parts[1], TIME_FORMATTER);
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException(
                    "فرمت ساعت باید مانند 08:30 باشد."
            );
        }

        return LocalDateTime.of(date, time);
    }

    public LocalDate toGregorian(
            int jalaliYear,
            int jalaliMonth,
            int jalaliDay
    ) {
        validateBasicJalaliDate(jalaliYear, jalaliMonth, jalaliDay);

        int[] gregorian = jalaliToGregorian(
                jalaliYear,
                jalaliMonth,
                jalaliDay
        );

        LocalDate result = LocalDate.of(
                gregorian[0],
                gregorian[1],
                gregorian[2]
        );

        int[] roundTrip = gregorianToJalali(
                result.getYear(),
                result.getMonthValue(),
                result.getDayOfMonth()
        );

        if (roundTrip[0] != jalaliYear
                || roundTrip[1] != jalaliMonth
                || roundTrip[2] != jalaliDay) {
            throw new IllegalArgumentException("تاریخ شمسی واردشده معتبر نیست.");
        }

        return result;
    }

    public int[] toJalali(LocalDate gregorianDate) {
        if (gregorianDate == null) {
            throw new IllegalArgumentException("تاریخ میلادی نمی‌تواند خالی باشد.");
        }

        return gregorianToJalali(
                gregorianDate.getYear(),
                gregorianDate.getMonthValue(),
                gregorianDate.getDayOfMonth()
        );
    }

    public int currentJalaliYear() {
        return toJalali(LocalDate.now())[0];
    }

    public int currentJalaliMonth() {
        return toJalali(LocalDate.now())[1];
    }

    public int currentJalaliQuarter() {
        return ((currentJalaliMonth() - 1) / 3) + 1;
    }

    public LocalDate startOfJalaliYear(int jalaliYear) {
        return toGregorian(jalaliYear, 1, 1);
    }

    public LocalDate endOfJalaliYear(int jalaliYear) {
        return toGregorian(jalaliYear + 1, 1, 1).minusDays(1);
    }

    public LocalDate[] jalaliQuarterRange(
            int jalaliYear,
            int quarter
    ) {
        if (quarter < 1 || quarter > 4) {
            throw new IllegalArgumentException("سه‌ماهه باید بین ۱ تا ۴ باشد.");
        }

        int startMonth = ((quarter - 1) * 3) + 1;
        LocalDate fromDate = toGregorian(jalaliYear, startMonth, 1);

        LocalDate toDate;

        if (quarter < 4) {
            toDate = toGregorian(jalaliYear, startMonth + 3, 1)
                    .minusDays(1);
        } else {
            toDate = toGregorian(jalaliYear + 1, 1, 1)
                    .minusDays(1);
        }

        return new LocalDate[]{fromDate, toDate};
    }

    public LocalDate[] currentJalaliMonthRange() {
        int[] today = toJalali(LocalDate.now());

        LocalDate fromDate = toGregorian(
                today[0],
                today[1],
                1
        );

        LocalDate toDate;

        if (today[1] == 12) {
            toDate = toGregorian(today[0] + 1, 1, 1)
                    .minusDays(1);
        } else {
            toDate = toGregorian(today[0], today[1] + 1, 1)
                    .minusDays(1);
        }

        LocalDate todayGregorian = LocalDate.now();
        if (toDate.isAfter(todayGregorian)) {
            toDate = todayGregorian;
        }

        return new LocalDate[]{fromDate, toDate};
    }

    public String quarterTitle(int quarter) {
        return switch (quarter) {
            case 1 -> "سه‌ماهه اول (فروردین تا خرداد)";
            case 2 -> "سه‌ماهه دوم (تیر تا شهریور)";
            case 3 -> "سه‌ماهه سوم (مهر تا آذر)";
            case 4 -> "سه‌ماهه چهارم (دی تا اسفند)";
            default -> throw new IllegalArgumentException(
                    "سه‌ماهه باید بین ۱ تا ۴ باشد."
            );
        };
    }

    private void validateBasicJalaliDate(
            int year,
            int month,
            int day
    ) {
        if (year < 1) {
            throw new IllegalArgumentException("سال شمسی معتبر نیست.");
        }

        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("ماه شمسی باید بین ۱ تا ۱۲ باشد.");
        }

        int maxDay;

        if (month <= 6) {
            maxDay = 31;
        } else if (month <= 11) {
            maxDay = 30;
        } else {
            maxDay = 30;
        }

        if (day < 1 || day > maxDay) {
            throw new IllegalArgumentException("روز واردشده برای این ماه معتبر نیست.");
        }
    }

    private String normalizeDigits(String value) {
        return value
                .replace('۰', '0')
                .replace('۱', '1')
                .replace('۲', '2')
                .replace('۳', '3')
                .replace('۴', '4')
                .replace('۵', '5')
                .replace('۶', '6')
                .replace('۷', '7')
                .replace('۸', '8')
                .replace('۹', '9')
                .replace('٠', '0')
                .replace('١', '1')
                .replace('٢', '2')
                .replace('٣', '3')
                .replace('٤', '4')
                .replace('٥', '5')
                .replace('٦', '6')
                .replace('٧', '7')
                .replace('٨', '8')
                .replace('٩', '9');
    }

    private int[] gregorianToJalali(
            int year,
            int month,
            int day
    ) {
        int[] gregorianDays = {
                0, 31, 59, 90, 120, 151,
                181, 212, 243, 273, 304, 334
        };

        int jalaliYear;

        if (year > 1600) {
            jalaliYear = 979;
            year -= 1600;
        } else {
            jalaliYear = 0;
            year -= 621;
        }

        int adjustedYear = month > 2 ? year + 1 : year;

        int days = (365 * year)
                + ((adjustedYear + 3) / 4)
                - ((adjustedYear + 99) / 100)
                + ((adjustedYear + 399) / 400)
                - 80
                + day
                + gregorianDays[month - 1];

        jalaliYear += 33 * (days / 12053);
        days %= 12053;

        jalaliYear += 4 * (days / 1461);
        days %= 1461;

        if (days > 365) {
            jalaliYear += (days - 1) / 365;
            days = (days - 1) % 365;
        }

        int jalaliMonth;
        int jalaliDay;

        if (days < 186) {
            jalaliMonth = 1 + (days / 31);
            jalaliDay = 1 + (days % 31);
        } else {
            jalaliMonth = 7 + ((days - 186) / 30);
            jalaliDay = 1 + ((days - 186) % 30);
        }

        return new int[]{
                jalaliYear,
                jalaliMonth,
                jalaliDay
        };
    }

    private int[] jalaliToGregorian(
            int year,
            int month,
            int day
    ) {
        year += 1595;

        int days = -355668
                + (365 * year)
                + ((year / 33) * 8)
                + (((year % 33) + 3) / 4)
                + day;

        if (month < 7) {
            days += (month - 1) * 31;
        } else {
            days += ((month - 7) * 30) + 186;
        }

        int gregorianYear = 400 * (days / 146097);
        days %= 146097;

        if (days > 36524) {
            gregorianYear += 100 * (--days / 36524);
            days %= 36524;

            if (days >= 365) {
                days++;
            }
        }

        gregorianYear += 4 * (days / 1461);
        days %= 1461;

        if (days > 365) {
            gregorianYear += (days - 1) / 365;
            days = (days - 1) % 365;
        }

        int gregorianDay = days + 1;

        int[] monthDays = {
                0,
                31,
                isGregorianLeapYear(gregorianYear) ? 29 : 28,
                31,
                30,
                31,
                30,
                31,
                31,
                30,
                31,
                30,
                31
        };

        int gregorianMonth;

        for (gregorianMonth = 1;
             gregorianMonth <= 12
                     && gregorianDay > monthDays[gregorianMonth];
             gregorianMonth++) {
            gregorianDay -= monthDays[gregorianMonth];
        }

        return new int[]{
                gregorianYear,
                gregorianMonth,
                gregorianDay
        };
    }

    private boolean isGregorianLeapYear(int year) {
        return (year % 4 == 0 && year % 100 != 0)
                || year % 400 == 0;
    }
}
