package ir.ac.sutech.attendance_system.service;

import ir.ac.sutech.attendance_system.dto.AttendanceSummaryReportRow;
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
import java.util.List;

@Component
public class AttendanceSummaryExcelExporter {

    private static final String[] HEADERS = {
            "ردیف",
            "نام کارمند",
            "کد پرسنلی",
            "واحد سازمانی",
            "روزهای حضور",
            "روزهای غیبت",
            "تردد ناقص",
            "بدون شیفت",
            "مجموع کارکرد",
            "مجموع تأخیر",
            "مجموع خروج زودهنگام"
    };

    private final JalaliDateUtil jalaliDateUtil;

    public AttendanceSummaryExcelExporter(
            JalaliDateUtil jalaliDateUtil
    ) {
        this.jalaliDateUtil = jalaliDateUtil;
    }

    public byte[] export(
            List<AttendanceSummaryReportRow> summaryRows,
            LocalDate fromDate,
            LocalDate toDate
    ) throws IOException {

        try (
                XSSFWorkbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream outputStream =
                        new ByteArrayOutputStream()
        ) {
            XSSFSheet sheet =
                    workbook.createSheet("خلاصه کارکرد");

            sheet.setRightToLeft(true);
            sheet.createFreezePane(0, 4);
            sheet.setFitToPage(true);

            sheet.getPrintSetup().setLandscape(true);
            sheet.getPrintSetup().setFitWidth((short) 1);
            sheet.getPrintSetup().setFitHeight((short) 0);

            workbook.getProperties()
                    .getCoreProperties()
                    .setTitle("گزارش خلاصه کارکرد کارکنان");

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

            CellStyle numberStyle =
                    createDataStyle(
                            workbook,
                            HorizontalAlignment.CENTER
                    );

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

            headerRow.setHeightInPoints(30);

            for (int columnIndex = 0;
                 columnIndex < HEADERS.length;
                 columnIndex++) {

                Cell cell =
                        headerRow.createCell(columnIndex);

                cell.setCellValue(HEADERS[columnIndex]);
                cell.setCellStyle(headerStyle);
            }

            int currentRowIndex = headerRowIndex + 1;

            long totalPresentDays = 0;
            long totalAbsentDays = 0;
            long totalIncompleteDays = 0;
            long totalNoShiftDays = 0;
            long totalWorkedMinutes = 0;
            long totalLateMinutes = 0;
            long totalEarlyLeaveMinutes = 0;

            for (int index = 0;
                 index < summaryRows.size();
                 index++) {

                AttendanceSummaryReportRow reportRow =
                        summaryRows.get(index);

                Employee employee =
                        reportRow.getEmployee();

                Row excelRow =
                        sheet.createRow(currentRowIndex++);

                excelRow.setHeightInPoints(25);

                createNumericCell(
                        excelRow,
                        0,
                        index + 1,
                        numberStyle
                );

                String employeeName =
                        safeText(employee.getFirstName())
                                + " "
                                + safeText(employee.getLastName());

                createTextCell(
                        excelRow,
                        1,
                        employeeName.trim(),
                        textStyle
                );

                createTextCell(
                        excelRow,
                        2,
                        safeText(employee.getPersonnelCode()),
                        numberStyle
                );

                String departmentName = "—";

                if (employee.getDepartment() != null) {
                    departmentName = safeText(
                            employee.getDepartment().getName()
                    );
                }

                createTextCell(
                        excelRow,
                        3,
                        departmentName,
                        textStyle
                );

                createNumericCell(
                        excelRow,
                        4,
                        reportRow.getPresentDays(),
                        numberStyle
                );

                createNumericCell(
                        excelRow,
                        5,
                        reportRow.getAbsentDays(),
                        numberStyle
                );

                createNumericCell(
                        excelRow,
                        6,
                        reportRow.getIncompleteDays(),
                        numberStyle
                );

                createNumericCell(
                        excelRow,
                        7,
                        reportRow.getNoShiftDays(),
                        numberStyle
                );

                createDurationCell(
                        excelRow,
                        8,
                        reportRow.getTotalWorkedMinutes(),
                        durationStyle
                );

                createDurationCell(
                        excelRow,
                        9,
                        reportRow.getTotalLateMinutes(),
                        durationStyle
                );

                createDurationCell(
                        excelRow,
                        10,
                        reportRow.getTotalEarlyLeaveMinutes(),
                        durationStyle
                );

                totalPresentDays +=
                        reportRow.getPresentDays();

                totalAbsentDays +=
                        reportRow.getAbsentDays();

                totalIncompleteDays +=
                        reportRow.getIncompleteDays();

                totalNoShiftDays +=
                        reportRow.getNoShiftDays();

                totalWorkedMinutes +=
                        reportRow.getTotalWorkedMinutes();

                totalLateMinutes +=
                        reportRow.getTotalLateMinutes();

                totalEarlyLeaveMinutes +=
                        reportRow.getTotalEarlyLeaveMinutes();
            }

            createTotalRow(
                    sheet,
                    currentRowIndex,
                    totalPresentDays,
                    totalAbsentDays,
                    totalIncompleteDays,
                    totalNoShiftDays,
                    totalWorkedMinutes,
                    totalLateMinutes,
                    totalEarlyLeaveMinutes,
                    totalStyle,
                    totalDurationStyle
            );

            int lastFilterRow =
                    Math.max(
                            headerRowIndex,
                            currentRowIndex - 1
                    );

            sheet.setAutoFilter(
                    new CellRangeAddress(
                            headerRowIndex,
                            lastFilterRow,
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
        titleRow.setHeightInPoints(34);

        Cell titleCell = titleRow.createCell(0);

        titleCell.setCellValue(
                "گزارش خلاصه کارکرد کارکنان"
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
        subtitleRow.setHeightInPoints(24);

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
            long presentDays,
            long absentDays,
            long incompleteDays,
            long noShiftDays,
            long workedMinutes,
            long lateMinutes,
            long earlyLeaveMinutes,
            CellStyle totalStyle,
            CellStyle totalDurationStyle
    ) {
        Row totalRow = sheet.createRow(rowIndex);
        totalRow.setHeightInPoints(28);

        createTextCell(
                totalRow,
                0,
                "جمع کل",
                totalStyle
        );

        for (int columnIndex = 1;
             columnIndex <= 3;
             columnIndex++) {

            createTextCell(
                    totalRow,
                    columnIndex,
                    "",
                    totalStyle
            );
        }

        createNumericCell(
                totalRow,
                4,
                presentDays,
                totalStyle
        );

        createNumericCell(
                totalRow,
                5,
                absentDays,
                totalStyle
        );

        createNumericCell(
                totalRow,
                6,
                incompleteDays,
                totalStyle
        );

        createNumericCell(
                totalRow,
                7,
                noShiftDays,
                totalStyle
        );

        createDurationCell(
                totalRow,
                8,
                workedMinutes,
                totalDurationStyle
        );

        createDurationCell(
                totalRow,
                9,
                lateMinutes,
                totalDurationStyle
        );

        createDurationCell(
                totalRow,
                10,
                earlyLeaveMinutes,
                totalDurationStyle
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
                workbook.createDataFormat().getFormat("[h]:mm")
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
                24,
                16,
                22,
                15,
                15,
                15,
                15,
                22,
                22,
                26
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