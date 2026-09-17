package ir.ac.sutech.attendance_system.service;

import ir.ac.sutech.attendance_system.dto.DailyAttendanceReportRow;
import ir.ac.sutech.attendance_system.model.Employee;
import ir.ac.sutech.attendance_system.util.JalaliDateUtil;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class DailyAttendanceExcelExporter {

    private static final String[] HEADERS = {
            "ردیف",
            "تاریخ",
            "نام کارمند",
            "کد پرسنلی",
            "واحد سازمانی",
            "شیفت",
            "شروع شیفت",
            "پایان شیفت",
            "اولین ورود",
            "آخرین خروج",
            "مجموع کارکرد",
            "استراحت (دقیقه)",
            "اضافه‌کاری",
            "تأخیر (دقیقه)",
            "خروج زودهنگام (دقیقه)",
            "وضعیت"
    };

    private final JalaliDateUtil jalaliDateUtil;

    public DailyAttendanceExcelExporter(
            JalaliDateUtil jalaliDateUtil
    ) {
        this.jalaliDateUtil = jalaliDateUtil;
    }

    public byte[] export(
            List<DailyAttendanceReportRow> reportRows,
            LocalDate fromDate,
            LocalDate toDate
    ) throws IOException {

        try (
                XSSFWorkbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream outputStream =
                        new ByteArrayOutputStream()
        ) {
            XSSFSheet sheet =
                    workbook.createSheet("گزارش روزانه");

            sheet.setRightToLeft(true);
            sheet.setDisplayGridlines(false);
            sheet.createFreezePane(0, 4);
            sheet.setFitToPage(true);

            sheet.getPrintSetup().setLandscape(true);
            sheet.getPrintSetup().setFitWidth((short) 1);
            sheet.getPrintSetup().setFitHeight((short) 0);

            workbook.getProperties()
                    .getCoreProperties()
                    .setTitle(
                            "گزارش روزانه و شیفتی حضور و غیاب"
                    );

            CellStyle titleStyle =
                    createTitleStyle(workbook);

            CellStyle subtitleStyle =
                    createSubtitleStyle(workbook);

            CellStyle headerStyle =
                    createHeaderStyle(workbook);

            CellStyle textStyle =
                    createDataStyle(
                            workbook,
                            HorizontalAlignment.RIGHT
                    );

            CellStyle centerStyle =
                    createDataStyle(
                            workbook,
                            HorizontalAlignment.CENTER
                    );

            CellStyle dateStyle =
                    createDateStyle(workbook);

            CellStyle timeStyle =
                    createTimeStyle(workbook);

            CellStyle durationStyle =
                    createDurationStyle(workbook, false);

            CellStyle totalStyle =
                    createTotalStyle(workbook);

            CellStyle totalDurationStyle =
                    createDurationStyle(workbook, true);

            createTitleRows(
                    sheet,
                    fromDate,
                    toDate,
                    titleStyle,
                    subtitleStyle
            );

            int headerRowIndex = 3;

            Row headerRow =
                    sheet.createRow(headerRowIndex);

            headerRow.setHeightInPoints(34);

            for (int columnIndex = 0;
                 columnIndex < HEADERS.length;
                 columnIndex++) {

                Cell cell =
                        headerRow.createCell(columnIndex);

                cell.setCellValue(HEADERS[columnIndex]);
                cell.setCellStyle(headerStyle);
            }

            int currentRowIndex = headerRowIndex + 1;

            long totalWorkedMinutes = 0;
            long totalOvertimeMinutes = 0;
            long totalLateMinutes = 0;
            long totalEarlyLeaveMinutes = 0;

            for (int index = 0;
                 index < reportRows.size();
                 index++) {

                DailyAttendanceReportRow reportRow =
                        reportRows.get(index);

                Employee employee =
                        reportRow.getEmployee();

                Row excelRow =
                        sheet.createRow(currentRowIndex++);

                excelRow.setHeightInPoints(25);

                createNumericCell(
                        excelRow,
                        0,
                        index + 1,
                        centerStyle
                );

                createTextCell(
                        excelRow,
                        1,
                        jalaliDateUtil.format(
                                reportRow.getReportDate()
                        ),
                        centerStyle
                );

                String employeeName =
                        safeText(employee.getFirstName())
                                + " "
                                + safeText(employee.getLastName());

                createTextCell(
                        excelRow,
                        2,
                        employeeName.trim(),
                        textStyle
                );

                createTextCell(
                        excelRow,
                        3,
                        safeText(employee.getPersonnelCode()),
                        centerStyle
                );

                String departmentName = "—";

                if (employee.getDepartment() != null) {
                    departmentName = safeText(
                            employee.getDepartment().getName()
                    );
                }

                createTextCell(
                        excelRow,
                        4,
                        departmentName,
                        textStyle
                );

                String shiftName = "—";

                if (reportRow.getShift() != null) {
                    shiftName = safeText(
                            reportRow.getShift().getName()
                    );
                }

                createTextCell(
                        excelRow,
                        5,
                        shiftName,
                        centerStyle
                );

                createTimeCell(
                        excelRow,
                        6,
                        reportRow.getScheduledStart(),
                        timeStyle,
                        centerStyle
                );

                createTimeCell(
                        excelRow,
                        7,
                        reportRow.getScheduledEnd(),
                        timeStyle,
                        centerStyle
                );

                createTimeCell(
                        excelRow,
                        8,
                        reportRow.getFirstCheckIn(),
                        timeStyle,
                        centerStyle
                );

                createTimeCell(
                        excelRow,
                        9,
                        reportRow.getLastCheckOut(),
                        timeStyle,
                        centerStyle
                );

                if (
                        reportRow.hasCheckIn()
                                && reportRow.hasCheckOut()
                ) {
                    createDurationCell(
                            excelRow,
                            10,
                            reportRow.getWorkedMinutes(),
                            durationStyle
                    );
                } else {
                    createTextCell(
                            excelRow,
                            10,
                            "—",
                            centerStyle
                    );
                }

                createNumericCell(
                        excelRow,
                        11,
                        reportRow.getBreakMinutes(),
                        centerStyle
                );

                createDurationCell(
                        excelRow,
                        12,
                        reportRow.getOvertimeMinutes(),
                        durationStyle
                );

                createNumericCell(
                        excelRow,
                        13,
                        reportRow.getLateMinutes(),
                        centerStyle
                );

                createNumericCell(
                        excelRow,
                        14,
                        reportRow.getEarlyLeaveMinutes(),
                        centerStyle
                );

                createTextCell(
                        excelRow,
                        15,
                        reportRow.getStatusTitle(),
                        centerStyle
                );

                totalWorkedMinutes +=
                        reportRow.getWorkedMinutes();

                totalOvertimeMinutes +=
                        reportRow.getOvertimeMinutes();

                totalLateMinutes +=
                        reportRow.getLateMinutes();

                totalEarlyLeaveMinutes +=
                        reportRow.getEarlyLeaveMinutes();
            }

            int lastDataRowIndex = currentRowIndex - 1;

            createTotalRow(
                    sheet,
                    currentRowIndex,
                    reportRows.size(),
                    totalWorkedMinutes,
                    totalOvertimeMinutes,
                    totalLateMinutes,
                    totalEarlyLeaveMinutes,
                    totalStyle,
                    totalDurationStyle
            );

            sheet.setAutoFilter(
                    new CellRangeAddress(
                            headerRowIndex,
                            Math.max(
                                    headerRowIndex,
                                    lastDataRowIndex
                            ),
                            0,
                            HEADERS.length - 1
                    )
            );

            setColumnWidths(sheet);

            workbook.write(outputStream);

            return outputStream.toByteArray();
        }
    }

    private void createTitleRows(
            XSSFSheet sheet,
            LocalDate fromDate,
            LocalDate toDate,
            CellStyle titleStyle,
            CellStyle subtitleStyle
    ) {
        Row titleRow = sheet.createRow(0);
        titleRow.setHeightInPoints(36);

        Cell titleCell = titleRow.createCell(0);

        titleCell.setCellValue(
                "گزارش روزانه و شیفتی حضور و غیاب کارکنان"
        );

        titleCell.setCellStyle(titleStyle);

        sheet.addMergedRegion(
                new CellRangeAddress(
                        0,
                        0,
                        0,
                        HEADERS.length - 1
                )
        );

        Row subtitleRow = sheet.createRow(1);
        subtitleRow.setHeightInPoints(25);

        Cell subtitleCell =
                subtitleRow.createCell(0);

        subtitleCell.setCellValue(
                "بازه گزارش: "
                        + jalaliDateUtil.format(fromDate)
                        + " تا "
                        + jalaliDateUtil.format(toDate)
        );

        subtitleCell.setCellStyle(subtitleStyle);

        sheet.addMergedRegion(
                new CellRangeAddress(
                        1,
                        1,
                        0,
                        HEADERS.length - 1
                )
        );
    }

    private void createTotalRow(
            XSSFSheet sheet,
            int rowIndex,
            long rowCount,
            long workedMinutes,
            long overtimeMinutes,
            long lateMinutes,
            long earlyLeaveMinutes,
            CellStyle totalStyle,
            CellStyle totalDurationStyle
    ) {
        Row totalRow = sheet.createRow(rowIndex);
        totalRow.setHeightInPoints(29);

        for (int columnIndex = 0;
             columnIndex < HEADERS.length;
             columnIndex++) {

            createTextCell(
                    totalRow,
                    columnIndex,
                    "",
                    totalStyle
            );
        }

        totalRow.getCell(0).setCellValue(
                "جمع کل " + rowCount + " ردیف"
        );

        sheet.addMergedRegion(
                new CellRangeAddress(
                        rowIndex,
                        rowIndex,
                        0,
                        9
                )
        );

        createDurationCell(
                totalRow,
                10,
                workedMinutes,
                totalDurationStyle
        );

        createNumericCell(
                totalRow,
                11,
                0,
                totalStyle
        );

        createDurationCell(
                totalRow,
                12,
                overtimeMinutes,
                totalDurationStyle
        );

        createNumericCell(
                totalRow,
                13,
                lateMinutes,
                totalStyle
        );

        createNumericCell(
                totalRow,
                14,
                earlyLeaveMinutes,
                totalStyle
        );
    }

    private CellStyle createTitleStyle(
            XSSFWorkbook workbook
    ) {
        CellStyle style =
                workbook.createCellStyle();

        Font font = workbook.createFont();

        font.setFontName("Tahoma");
        font.setFontHeightInPoints((short) 16);
        font.setBold(true);
        font.setColor(
                IndexedColors.WHITE.getIndex()
        );

        style.setFont(font);

        style.setAlignment(
                HorizontalAlignment.CENTER
        );

        style.setVerticalAlignment(
                VerticalAlignment.CENTER
        );

        style.setFillForegroundColor(
                IndexedColors.DARK_BLUE.getIndex()
        );

        style.setFillPattern(
                FillPatternType.SOLID_FOREGROUND
        );

        return style;
    }

    private CellStyle createSubtitleStyle(
            XSSFWorkbook workbook
    ) {
        CellStyle style =
                workbook.createCellStyle();

        Font font = workbook.createFont();

        font.setFontName("Tahoma");
        font.setFontHeightInPoints((short) 11);

        font.setColor(
                IndexedColors.DARK_BLUE.getIndex()
        );

        style.setFont(font);

        style.setAlignment(
                HorizontalAlignment.CENTER
        );

        style.setVerticalAlignment(
                VerticalAlignment.CENTER
        );

        style.setFillForegroundColor(
                IndexedColors.LIGHT_CORNFLOWER_BLUE
                        .getIndex()
        );

        style.setFillPattern(
                FillPatternType.SOLID_FOREGROUND
        );

        return style;
    }

    private CellStyle createHeaderStyle(
            XSSFWorkbook workbook
    ) {
        CellStyle style =
                workbook.createCellStyle();

        Font font = workbook.createFont();

        font.setFontName("Tahoma");
        font.setFontHeightInPoints((short) 10);
        font.setBold(true);

        font.setColor(
                IndexedColors.WHITE.getIndex()
        );

        style.setFont(font);

        style.setAlignment(
                HorizontalAlignment.CENTER
        );

        style.setVerticalAlignment(
                VerticalAlignment.CENTER
        );

        style.setWrapText(true);

        style.setFillForegroundColor(
                IndexedColors.BLUE_GREY.getIndex()
        );

        style.setFillPattern(
                FillPatternType.SOLID_FOREGROUND
        );

        applyBorders(style);

        return style;
    }

    private CellStyle createDataStyle(
            XSSFWorkbook workbook,
            HorizontalAlignment alignment
    ) {
        CellStyle style =
                workbook.createCellStyle();

        Font font = workbook.createFont();

        font.setFontName("Tahoma");
        font.setFontHeightInPoints((short) 10);

        style.setFont(font);
        style.setAlignment(alignment);

        style.setVerticalAlignment(
                VerticalAlignment.CENTER
        );

        applyBorders(style);

        return style;
    }

    private CellStyle createDateStyle(
            XSSFWorkbook workbook
    ) {
        CellStyle style =
                createDataStyle(
                        workbook,
                        HorizontalAlignment.CENTER
                );

        style.setDataFormat(
                workbook.createDataFormat()
                        .getFormat("yyyy/mm/dd")
        );

        return style;
    }

    private CellStyle createTimeStyle(
            XSSFWorkbook workbook
    ) {
        CellStyle style =
                createDataStyle(
                        workbook,
                        HorizontalAlignment.CENTER
                );

        style.setDataFormat(
                workbook.createDataFormat()
                        .getFormat("hh:mm")
        );

        return style;
    }

    private CellStyle createDurationStyle(
            XSSFWorkbook workbook,
            boolean total
    ) {
        CellStyle style =
                workbook.createCellStyle();

        Font font = workbook.createFont();

        font.setFontName("Tahoma");
        font.setFontHeightInPoints((short) 10);
        font.setBold(total);

        style.setFont(font);

        style.setAlignment(
                HorizontalAlignment.CENTER
        );

        style.setVerticalAlignment(
                VerticalAlignment.CENTER
        );

        style.setDataFormat(
                workbook.createDataFormat()
                        .getFormat("[h]:mm")
        );

        if (total) {
            style.setFillForegroundColor(
                    IndexedColors.LIGHT_YELLOW.getIndex()
            );

            style.setFillPattern(
                    FillPatternType.SOLID_FOREGROUND
            );
        }

        applyBorders(style);

        return style;
    }

    private CellStyle createTotalStyle(
            XSSFWorkbook workbook
    ) {
        CellStyle style =
                workbook.createCellStyle();

        Font font = workbook.createFont();

        font.setFontName("Tahoma");
        font.setFontHeightInPoints((short) 10);
        font.setBold(true);

        style.setFont(font);

        style.setAlignment(
                HorizontalAlignment.CENTER
        );

        style.setVerticalAlignment(
                VerticalAlignment.CENTER
        );

        style.setFillForegroundColor(
                IndexedColors.LIGHT_YELLOW.getIndex()
        );

        style.setFillPattern(
                FillPatternType.SOLID_FOREGROUND
        );

        applyBorders(style);

        return style;
    }

    private void applyBorders(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }

    private void createTextCell(
            Row row,
            int columnIndex,
            String value,
            CellStyle style
    ) {
        Cell cell = row.createCell(columnIndex);

        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private void createNumericCell(
            Row row,
            int columnIndex,
            long value,
            CellStyle style
    ) {
        Cell cell = row.createCell(columnIndex);

        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private void createDateCell(
            Row row,
            int columnIndex,
            LocalDate value,
            CellStyle style
    ) {
        Cell cell = row.createCell(columnIndex);

        if (value == null) {
            cell.setCellValue("—");
        } else {
            cell.setCellValue(value);
        }

        cell.setCellStyle(style);
    }

    private void createTimeCell(
            Row row,
            int columnIndex,
            LocalDateTime value,
            CellStyle timeStyle,
            CellStyle emptyStyle
    ) {
        Cell cell = row.createCell(columnIndex);

        if (value == null) {
            cell.setCellValue("—");
            cell.setCellStyle(emptyStyle);
        } else {
            cell.setCellValue(value);
            cell.setCellStyle(timeStyle);
        }
    }

    private void createDurationCell(
            Row row,
            int columnIndex,
            long minutes,
            CellStyle style
    ) {
        Cell cell = row.createCell(columnIndex);

        cell.setCellValue(
                Math.max(minutes, 0) / 1440.0
        );

        cell.setCellStyle(style);
    }

    private String safeText(Object value) {
        return value == null
                ? ""
                : String.valueOf(value);
    }

    private void setColumnWidths(
            XSSFSheet sheet
    ) {
        int[] widths = {
                8,
                14,
                24,
                16,
                22,
                18,
                14,
                14,
                14,
                14,
                18,
                16,
                18,
                18,
                25,
                17
        };

        for (int columnIndex = 0;
             columnIndex < widths.length;
             columnIndex++) {

            sheet.setColumnWidth(
                    columnIndex,
                    widths[columnIndex] * 256
            );
        }
    }
}