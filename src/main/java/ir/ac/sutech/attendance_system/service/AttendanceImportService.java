package ir.ac.sutech.attendance_system.service;

import ir.ac.sutech.attendance_system.dto.AttendanceImportResult;
import ir.ac.sutech.attendance_system.model.AttendanceRecord;
import ir.ac.sutech.attendance_system.model.AttendanceSource;
import ir.ac.sutech.attendance_system.model.AttendanceType;
import ir.ac.sutech.attendance_system.model.Employee;
import ir.ac.sutech.attendance_system.repository.AttendanceRecordRepository;
import ir.ac.sutech.attendance_system.repository.EmployeeRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.nio.charset.StandardCharsets;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class AttendanceImportService {

    private static final List<DateTimeFormatter>
            DATE_FORMATS = List.of(

            DateTimeFormatter.ISO_LOCAL_DATE_TIME,

            DateTimeFormatter.ofPattern(
                    "yyyy-MM-dd HH:mm:ss"
            ),

            DateTimeFormatter.ofPattern(
                    "yyyy/MM/dd HH:mm:ss"
            ),

            DateTimeFormatter.ofPattern(
                    "yyyy-MM-dd HH:mm"
            )
    );

    private final AttendanceRecordRepository
            recordRepository;

    private final EmployeeRepository
            employeeRepository;

    public AttendanceImportService(
            AttendanceRecordRepository recordRepository,
            EmployeeRepository employeeRepository
    ) {
        this.recordRepository = recordRepository;
        this.employeeRepository = employeeRepository;
    }

    @Transactional
    public AttendanceImportResult importCsv(
            MultipartFile file,
            String requestedDeviceCode
    ) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "فایل CSV را انتخاب کنید."
            );
        }

        String deviceCode =
                normalize(requestedDeviceCode);

        if (deviceCode == null) {
            throw new IllegalArgumentException(
                    "کد دستگاه را وارد کنید."
            );
        }

        int imported = 0;
        int duplicates = 0;
        int rejected = 0;

        List<String> errors = new ArrayList<>();

        Set<String> seenRecordIds =
                new HashSet<>();

        try (
                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(
                                        file.getInputStream(),
                                        StandardCharsets.UTF_8
                                )
                        )
        ) {
            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;

                line = line
                        .replace("\uFEFF", "")
                        .trim();

                if (line.isBlank()
                        || lineNumber == 1
                        && isHeader(line)) {

                    continue;
                }

                try {
                    String[] columns =
                            line.split("[,;]", -1);

                    if (columns.length < 4) {
                        throw new IllegalArgumentException(
                                "چهار ستون لازم است"
                        );
                    }

                    String deviceUserId = require(
                            columns[0],
                            "شناسه کارمند"
                    );

                    LocalDateTime eventTime =
                            parseDateTime(columns[1]);

                    AttendanceType type =
                            parseType(columns[2]);

                    String deviceRecordId = require(
                            columns[3],
                            "شناسه رکورد"
                    );

                    String duplicateKey =
                            deviceCode
                                    + "|"
                                    + deviceRecordId;

                    if (!seenRecordIds.add(duplicateKey)
                            || recordRepository
                            .existsByDeviceCodeAndDeviceRecordId(
                                    deviceCode,
                                    deviceRecordId
                            )) {

                        duplicates++;
                        continue;
                    }

                    Employee employee =
                            employeeRepository
                                    .findByDeviceUserId(
                                            deviceUserId
                                    )
                                    .orElseThrow(
                                            () ->
                                                    new IllegalArgumentException(
                                                            "کارمند با شناسه دستگاه "
                                                                    + deviceUserId
                                                                    + " پیدا نشد"
                                                    )
                                    );

                    if (!employee.isActive()) {
                        throw new IllegalArgumentException(
                                "کارمند غیرفعال است"
                        );
                    }

                    AttendanceRecord record =
                            new AttendanceRecord();

                    record.setEmployee(employee);

                    record.setEventTime(eventTime);

                    record.setAttendanceType(type);

                    record.setAttendanceSource(
                            AttendanceSource.FILE_IMPORT
                    );

                    record.setDeviceCode(deviceCode);

                    record.setDeviceRecordId(
                            deviceRecordId
                    );

                    recordRepository.save(record);

                    imported++;

                } catch (
                        IllegalArgumentException exception
                ) {
                    rejected++;

                    if (errors.size() < 12) {
                        errors.add(
                                "سطر "
                                        + lineNumber
                                        + ": "
                                        + exception.getMessage()
                        );
                    }
                }
            }

        } catch (IOException exception) {
            throw new IllegalArgumentException(
                    "خواندن فایل ممکن نشد.",
                    exception
            );
        }

        return new AttendanceImportResult(
                imported,
                duplicates,
                rejected,
                List.copyOf(errors)
        );
    }

    private boolean isHeader(String line) {
        String lower =
                line.toLowerCase(Locale.ROOT);

        return lower.contains("device_user")
                || lower.contains("event_time");
    }

    private String require(
            String value,
            String title
    ) {
        String normalized = normalize(value);

        if (normalized == null) {
            throw new IllegalArgumentException(
                    title + " خالی است"
            );
        }

        return normalized;
    }

    private String normalize(String value) {
        return value == null || value.isBlank()
                ? null
                : value.trim();
    }

    private LocalDateTime parseDateTime(
            String value
    ) {
        String normalized =
                require(value, "تاریخ و ساعت");

        for (
                DateTimeFormatter formatter
                : DATE_FORMATS
        ) {
            try {
                return LocalDateTime.parse(
                        normalized,
                        formatter
                );

            } catch (
                    DateTimeParseException ignored
            ) {
                // قالب بعدی بررسی می‌شود.
            }
        }

        throw new IllegalArgumentException(
                "قالب تاریخ و ساعت معتبر نیست"
        );
    }

    private AttendanceType parseType(
            String value
    ) {
        String normalized =
                require(value, "نوع تردد")
                        .toUpperCase(Locale.ROOT);

        return switch (normalized) {
            case "ENTRY", "IN", "ورود", "0" ->
                    AttendanceType.ENTRY;

            case "EXIT", "OUT", "خروج", "1" ->
                    AttendanceType.EXIT;

            default ->
                    throw new IllegalArgumentException(
                            "نوع تردد باید ورود یا خروج باشد"
                    );
        };
    }
}