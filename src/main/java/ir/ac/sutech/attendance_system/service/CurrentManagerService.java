package ir.ac.sutech.attendance_system.service;

import ir.ac.sutech.attendance_system.model.Employee;
import ir.ac.sutech.attendance_system.repository.EmployeeRepository;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import org.springframework.security.core.Authentication;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CurrentManagerService {

    private final JdbcTemplate jdbcTemplate;

    private final EmployeeRepository
            employeeRepository;


    public CurrentManagerService(
            JdbcTemplate jdbcTemplate,
            EmployeeRepository employeeRepository
    ) {

        this.jdbcTemplate =
                jdbcTemplate;

        this.employeeRepository =
                employeeRepository;
    }


    /*
     * ==========================================
     * دریافت مدیر بخش لاگین‌شده
     * ==========================================
     */

    @Transactional(readOnly = true)
    public Employee getCurrentManager(
            Authentication authentication
    ) {

        /*
         * کاربر باید Login شده باشد.
         */

        if (
                authentication == null
                        ||
                        !authentication.isAuthenticated()
                        ||
                        authentication.getName() == null
        ) {

            throw new IllegalStateException(
                    "کاربر وارد سامانه نشده است."
            );
        }


        String username =
                authentication.getName();


        Long employeeId;


        try {

            /*
             * شناسه Employee مرتبط با حساب کاربری
             */

            employeeId =
                    jdbcTemplate.queryForObject(
                            """
                            SELECT employee_id
                            FROM user_accounts
                            WHERE LOWER(username) = LOWER(?)
                              AND active = TRUE
                              AND employee_id IS NOT NULL
                            """,
                            Long.class,
                            username
                    );


        } catch (
                EmptyResultDataAccessException exception
        ) {

            throw new IllegalStateException(
                    "برای حساب مدیر بخش، کارمند مرتبطی تعریف نشده است."
            );
        }


        if (employeeId == null) {

            throw new IllegalStateException(
                    "حساب مدیر بخش به پرونده پرسنلی متصل نشده است."
            );
        }


        Employee manager =
                employeeRepository
                        .findById(
                                employeeId
                        )
                        .orElseThrow(() ->
                                new IllegalStateException(
                                        "اطلاعات مدیر بخش یافت نشد."
                                )
                        );


        /*
         * مدیر غیرفعال نباید پنل فعال داشته باشد.
         */

        if (!manager.isActive()) {

            throw new IllegalStateException(
                    "پرونده پرسنلی مدیر بخش غیرفعال است."
            );
        }


        /*
         * Department در Employee به صورت LAZY است.
         *
         * بنابراین داخل Transaction بارگذاری می‌شود
         * تا در Thymeleaf خطای LazyInitializationException
         * نداشته باشیم.
         */

        if (manager.getDepartment() == null) {

            throw new IllegalStateException(
                    "برای مدیر بخش، واحد سازمانی مشخص نشده است."
            );
        }


        manager
                .getDepartment()
                .getName();


        manager
                .getDepartment()
                .getCode();


        /*
         * در صورت وجود Shift
         */

        if (manager.getShift() != null) {

            manager
                    .getShift()
                    .getName();
        }


        return manager;
    }
}