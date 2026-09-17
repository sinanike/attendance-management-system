package ir.ac.sutech.attendance_system.service;

import ir.ac.sutech.attendance_system.dto.AttendanceSummaryReportRow;

import org.springframework.stereotype.Service;

import java.time.LocalDate;

import java.util.List;
import java.util.Locale;

@Service
public class AttendanceAiAnalysisService {

    private final GeminiService geminiService;

    public AttendanceAiAnalysisService(
            GeminiService geminiService
    ) {

        this.geminiService =
                geminiService;
    }

    public boolean isAvailable() {

        return geminiService
                .isAvailable();
    }

    public String analyze(

            List<AttendanceSummaryReportRow> rows,

            LocalDate fromDate,

            LocalDate toDate
    ) {

        if (
                rows == null
                        || rows.isEmpty()
        ) {

            return "برای بازه انتخاب‌شده داده‌ای جهت تحلیل وجود ندارد.";
        }

        /*
         * ==========================================
         * تعداد کارکنان
         * ==========================================
         */

        long employeeCount =
                rows.size();


        /*
         * ==========================================
         * آمار وضعیت‌های روزانه
         *
         * این مقادیر «نفر-روز» هستند.
         *
         * مثال:
         * اگر 7 کارمند در 2 روز تعطیل باشند:
         *
         * 7 × 2 = 14 نفر-روز تعطیلی رسمی
         * ==========================================
         */

        long totalPresentDays =
                rows.stream()
                        .mapToLong(
                                AttendanceSummaryReportRow
                                        ::getPresentDays
                        )
                        .sum();

        long totalAbsentDays =
                rows.stream()
                        .mapToLong(
                                AttendanceSummaryReportRow
                                        ::getAbsentDays
                        )
                        .sum();

        long totalIncompleteDays =
                rows.stream()
                        .mapToLong(
                                AttendanceSummaryReportRow
                                        ::getIncompleteDays
                        )
                        .sum();

        long totalLeaveDays =
                rows.stream()
                        .mapToLong(
                                AttendanceSummaryReportRow
                                        ::getLeaveDays
                        )
                        .sum();

        long totalMissionDays =
                rows.stream()
                        .mapToLong(
                                AttendanceSummaryReportRow
                                        ::getMissionDays
                        )
                        .sum();

        long totalHolidayDays =
                rows.stream()
                        .mapToLong(
                                AttendanceSummaryReportRow
                                        ::getHolidayDays
                        )
                        .sum();

        long totalNoShiftDays =
                rows.stream()
                        .mapToLong(
                                AttendanceSummaryReportRow
                                        ::getNoShiftDays
                        )
                        .sum();


        /*
         * ==========================================
         * زمان‌ها
         * ==========================================
         */

        long totalWorkedMinutes =
                rows.stream()
                        .mapToLong(
                                AttendanceSummaryReportRow
                                        ::getTotalWorkedMinutes
                        )
                        .sum();

        long totalLateMinutes =
                rows.stream()
                        .mapToLong(
                                AttendanceSummaryReportRow
                                        ::getTotalLateMinutes
                        )
                        .sum();

        long totalEarlyLeaveMinutes =
                rows.stream()
                        .mapToLong(
                                AttendanceSummaryReportRow
                                        ::getTotalEarlyLeaveMinutes
                        )
                        .sum();


        /*
         * ==========================================
         * تعداد کارکنان دارای وضعیت خاص
         * ==========================================
         */

        long employeesWithLate =
                rows.stream()
                        .filter(
                                row ->
                                        row.getTotalLateMinutes()
                                                > 0
                        )
                        .count();

        long employeesWithAbsence =
                rows.stream()
                        .filter(
                                row ->
                                        row.getAbsentDays()
                                                > 0
                        )
                        .count();

        long employeesWithLeave =
                rows.stream()
                        .filter(
                                row ->
                                        row.getLeaveDays()
                                                > 0
                        )
                        .count();

        long employeesWithMission =
                rows.stream()
                        .filter(
                                row ->
                                        row.getMissionDays()
                                                > 0
                        )
                        .count();

        long employeesWithIncompleteRecords =
                rows.stream()
                        .filter(
                                row ->
                                        row.getIncompleteDays()
                                                > 0
                        )
                        .count();


        /*
         * ==========================================
         * نرخ حضور
         *
         * فقط روزهایی که واقعاً نیاز به ارزیابی
         * حضور دارند وارد مخرج می‌شوند.
         *
         * LEAVE
         * MISSION
         * HOLIDAY
         * NO_SHIFT
         *
         * وارد نرخ حضور نمی‌شوند.
         * ==========================================
         */

        long attendanceEvaluationDays =

                totalPresentDays
                        + totalAbsentDays
                        + totalIncompleteDays;


        double attendanceRate =

                attendanceEvaluationDays == 0

                        ? 0

                        : totalPresentDays
                        * 100.0
                        / attendanceEvaluationDays;


        String attendanceRateText =

                String.format(
                        Locale.US,
                        "%.1f",
                        attendanceRate
                );


        /*
         * ==========================================
         * Prompt
         * ==========================================
         */

        String prompt = """

                شما نقش تحلیل‌گر منابع انسانی یک دانشگاه را دارید.

                بر اساس آمار حضور و غیاب زیر،
                یک گزارش مدیریتی دقیق، کوتاه،
                حرفه‌ای و قابل استفاده برای مدیر امور اداری
                به زبان فارسی تولید کن.


                قوانین بسیار مهم درباره داده‌ها:

                - هیچ اطلاعات هویتی کارکنان در اختیار شما نیست.

                - از حدس زدن نام، هویت، سمت یا مشخصات کارکنان خودداری کن.

                - از حدس زدن علت غیبت، تأخیر،
                  مرخصی یا مأموریت خودداری کن.

                - اگر علت یک وضعیت در داده‌ها وجود ندارد،
                  فقط پیشنهاد بررسی آن را بده.

                - چیزی خارج از داده‌های ارائه‌شده اختراع نکن.


                قوانین مربوط به وضعیت حضور و غیاب:

                - مرخصی تأییدشده غیبت نیست.

                - مأموریت تأییدشده غیبت نیست.

                - تعطیل رسمی غیبت نیست.

                - روز بدون شیفت غیبت نیست.

                - تردد ناقص یک وضعیت مستقل است
                  و نباید به عنوان غیبت قطعی گزارش شود.

                - نرخ حضور فقط بر اساس این سه وضعیت محاسبه شده است:

                  حاضر
                  غایب
                  تردد ناقص

                - مرخصی، مأموریت، تعطیل رسمی
                  و بدون شیفت در مخرج نرخ حضور قرار ندارند.


                قانون بسیار مهم درباره واحد شمارش:

                - تمام مقادیر مربوط به حضور،
                  غیبت،
                  تردد ناقص،
                  مرخصی،
                  مأموریت،
                  تعطیل رسمی
                  و بدون شیفت
                  به صورت «نفر-روز» هستند.

                - این مقادیر تعداد روزهای تقویمی مستقل نیستند.

                - مثال:
                  اگر 7 کارمند در 2 روز تعطیل رسمی باشند،
                  مقدار ثبت‌شده برابر با 14 نفر-روز تعطیلی رسمی است.

                - بنابراین هنگام بیان این آمار
                  حتماً از عبارت «نفر-روز» استفاده کن.

                - مثلاً نگو:
                  «14 روز تعطیل رسمی»

                - بلکه بگو:
                  «14 نفر-روز تعطیلی رسمی»


                قوانین مربوط به تاریخ:

                - بازه زمانی ارائه‌شده در پایین
                  بر اساس تاریخ میلادی است.

                - نام ماه شمسی را حدس نزن.

                - معادل شمسی تاریخ را حدس نزن.

                - مثلاً از روی بازه میلادی
                  عبارت‌هایی مانند
                  «مرداد 1405»
                  یا
                  «شهریور 1405»
                  تولید نکن.

                - فقط همان تاریخ شروع و پایان
                  ارائه‌شده در داده را ذکر کن.

                - تنها در صورتی مجاز به ذکر تاریخ شمسی هستی
                  که تاریخ شمسی به صورت صریح
                  در داده ورودی ارائه شده باشد.


                بازه گزارش:

                از %s تا %s


                تعداد کارکنان بررسی‌شده:

                %d نفر


                مجموع نفر-روز حضور:

                %d


                مجموع نفر-روز غیبت:

                %d


                مجموع نفر-روز تردد ناقص:

                %d


                مجموع نفر-روز مرخصی تأییدشده:

                %d


                مجموع نفر-روز مأموریت تأییدشده:

                %d


                مجموع نفر-روز تعطیلی رسمی:

                %d


                مجموع نفر-روز بدون شیفت:

                %d


                مجموع کارکرد ثبت‌شده:

                %d ساعت و %d دقیقه


                مجموع تأخیر:

                %d ساعت و %d دقیقه


                مجموع خروج زودهنگام:

                %d ساعت و %d دقیقه


                تعداد کارکنان دارای حداقل یک تأخیر:

                %d نفر


                تعداد کارکنان دارای حداقل یک غیبت:

                %d نفر


                تعداد کارکنان دارای حداقل یک مرخصی:

                %d نفر


                تعداد کارکنان دارای حداقل یک مأموریت:

                %d نفر


                تعداد کارکنان دارای حداقل یک تردد ناقص:

                %d نفر


                تعداد کل نفر-روزهای مشمول ارزیابی حضور:

                %d


                نرخ حضور در نفر-روزهای مشمول ارزیابی:

                %s درصد


                خروجی را دقیقاً با این ساختار ارائه کن:


                1. جمع‌بندی مدیریتی

                وضعیت کلی بازه را با استفاده از
                نرخ حضور،
                حضور،
                غیبت،
                مرخصی،
                مأموریت،
                تعطیلی رسمی
                و بدون شیفت خلاصه کن.


                2. نکات قابل توجه

                مهم‌ترین الگوهای قابل مشاهده
                در داده‌ها را بیان کن.

                بین «نفر-روز» و «تعداد کارکنان»
                تفاوت قائل شو.


                3. وضعیت مرخصی، مأموریت و غیبت

                آمار این سه وضعیت را مقایسه کن.

                مرخصی و مأموریت تأییدشده
                را غیبت تلقی نکن.


                4. وضعیت تأخیر و خروج زودهنگام

                مجموع زمان‌ها
                و تعداد کارکنان دارای تأخیر
                را گزارش کن.


                5. موارد نیازمند بررسی

                فقط مواردی را مطرح کن
                که مستقیماً از همین داده‌ها
                قابل استنباط هستند.

                علت قطعی برای هیچ وضعیت
                اعلام نکن.


                6. پیشنهادهای مدیریتی

                پیشنهادهای عملی،
                کوتاه
                و مرتبط با داده‌های ارائه‌شده بده.


                قوانین قالب‌بندی:

                - از Markdown استفاده نکن.

                - از علامت ** استفاده نکن.

                - از علامت # استفاده نکن.

                - عنوان‌ها متن ساده باشند.

                - شماره بخش‌ها از 1 تا 6 حفظ شوند.

                - بین بخش‌ها یک خط خالی باشد.

                - آمار نفر-روز را همیشه
                  با عبارت «نفر-روز» بیان کن.

                - تعداد کارکنان را با عبارت «نفر» بیان کن.

                - از تبدیل یا حدس تاریخ شمسی خودداری کن.

                - پاسخ حداکثر حدود 400 کلمه باشد.

                """
                .formatted(

                        fromDate,
                        toDate,

                        employeeCount,

                        totalPresentDays,
                        totalAbsentDays,
                        totalIncompleteDays,

                        totalLeaveDays,
                        totalMissionDays,
                        totalHolidayDays,
                        totalNoShiftDays,

                        totalWorkedMinutes / 60,
                        totalWorkedMinutes % 60,

                        totalLateMinutes / 60,
                        totalLateMinutes % 60,

                        totalEarlyLeaveMinutes / 60,
                        totalEarlyLeaveMinutes % 60,

                        employeesWithLate,
                        employeesWithAbsence,
                        employeesWithLeave,
                        employeesWithMission,
                        employeesWithIncompleteRecords,

                        attendanceEvaluationDays,

                        attendanceRateText
                );


        /*
         * ==========================================
         * ارسال به Gemini
         * ==========================================
         */

        String response =
                geminiService
                        .ask(prompt);


        /*
         * ==========================================
         * پاکسازی پاسخ
         * ==========================================
         */

        return cleanAiResponse(
                response
        );
    }


    /*
     * ==============================================
     * پاکسازی خروجی Gemini
     * ==============================================
     */

    private String cleanAiResponse(
            String response
    ) {

        if (
                response == null
                        || response.isBlank()
        ) {

            return "تحلیلی از سرویس هوش مصنوعی دریافت نشد.";
        }

        String cleaned =
                response;


        /*
         * Code blocks
         */

        cleaned =
                cleaned.replace(
                        "```text",
                        ""
                );

        cleaned =
                cleaned.replace(
                        "```markdown",
                        ""
                );

        cleaned =
                cleaned.replace(
                        "```",
                        ""
                );


        /*
         * Bold / Markdown
         */

        cleaned =
                cleaned.replace(
                        "**",
                        ""
                );

        cleaned =
                cleaned.replace(
                        "__",
                        ""
                );


        /*
         * Markdown headings
         */

        cleaned =
                cleaned.replaceAll(
                        "(?m)^\\s*#{1,6}\\s*",
                        ""
                );


        /*
         * Markdown bullets
         */

        cleaned =
                cleaned.replaceAll(
                        "(?m)^\\s*\\*\\s+",
                        "• "
                );


        /*
         * حذف فاصله انتهای خطوط
         */

        cleaned =
                cleaned.replaceAll(
                        "(?m)[ \\t]+$",
                        ""
                );


        /*
         * جلوگیری از فاصله‌های زیاد
         */

        cleaned =
                cleaned.replaceAll(
                        "\\n{3,}",
                        "\n\n"
                );


        return cleaned.trim();
    }
}