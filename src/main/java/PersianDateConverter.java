package ir.ac.sutech.attendancesystem.util;

import java.time.LocalDate;

public final class PersianDateConverter {

    private PersianDateConverter() {
    }

    public static String toPersian(LocalDate gregorianDate) {

        if (gregorianDate == null) {
            return "";
        }

        int[] result = gregorianToJalali(
                gregorianDate.getYear(),
                gregorianDate.getMonthValue(),
                gregorianDate.getDayOfMonth()
        );

        return String.format(
                "%04d/%02d/%02d",
                result[0],
                result[1],
                result[2]
        );
    }

    public static LocalDate toGregorian(String persianDate) {

        if (persianDate == null || persianDate.isBlank()) {
            throw new IllegalArgumentException(
                    "تاریخ شمسی نمی‌تواند خالی باشد."
            );
        }

        String normalizedDate = normalizeDigits(persianDate.trim())
                .replace("-", "/");

        String[] parts = normalizedDate.split("/");

        if (parts.length != 3) {
            throw new IllegalArgumentException(
                    "فرمت تاریخ شمسی باید مانند 1405/05/07 باشد."
            );
        }

        try {
            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            int day = Integer.parseInt(parts[2]);

            validatePersianDate(year, month, day);

            int[] result = jalaliToGregorian(year, month, day);

            LocalDate gregorianDate = LocalDate.of(
                    result[0],
                    result[1],
                    result[2]
            );

            int[] convertedBack = gregorianToJalali(
                    gregorianDate.getYear(),
                    gregorianDate.getMonthValue(),
                    gregorianDate.getDayOfMonth()
            );

            if (convertedBack[0] != year
                    || convertedBack[1] != month
                    || convertedBack[2] != day) {

                throw new IllegalArgumentException(
                        "تاریخ شمسی واردشده معتبر نیست."
                );
            }

            return gregorianDate;

        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "تاریخ شمسی باید فقط شامل عدد باشد."
            );
        }
    }

    private static void validatePersianDate(
            int year,
            int month,
            int day
    ) {

        if (year < 1) {
            throw new IllegalArgumentException(
                    "سال شمسی معتبر نیست."
            );
        }

        if (month < 1 || month > 12) {
            throw new IllegalArgumentException(
                    "ماه شمسی باید بین 1 تا 12 باشد."
            );
        }

        int maximumDay;

        if (month <= 6) {
            maximumDay = 31;
        } else if (month <= 11) {
            maximumDay = 30;
        } else {
            maximumDay = 30;
        }

        if (day < 1 || day > maximumDay) {
            throw new IllegalArgumentException(
                    "روز واردشده برای این ماه معتبر نیست."
            );
        }
    }

    private static String normalizeDigits(String value) {

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

    private static int[] gregorianToJalali(
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

    private static int[] jalaliToGregorian(
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

        boolean leapYear =
                gregorianYear % 4 == 0
                        && (gregorianYear % 100 != 0
                        || gregorianYear % 400 == 0);

        int[] monthDays = {
                0,
                31,
                leapYear ? 29 : 28,
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

        int gregorianMonth = 1;

        while (gregorianMonth <= 12
                && gregorianDay > monthDays[gregorianMonth]) {

            gregorianDay -= monthDays[gregorianMonth];
            gregorianMonth++;
        }

        return new int[]{
                gregorianYear,
                gregorianMonth,
                gregorianDay
        };
    }
}