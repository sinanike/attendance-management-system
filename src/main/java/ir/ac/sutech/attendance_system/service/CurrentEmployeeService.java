package ir.ac.sutech.attendance_system.service;

import ir.ac.sutech.attendance_system.model.Employee;
import ir.ac.sutech.attendance_system.repository.EmployeeRepository;

import org.springframework.dao.EmptyResultDataAccessException;

import org.springframework.jdbc.core.JdbcTemplate;

import org.springframework.security.core.Authentication;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;


@Service
public class CurrentEmployeeService {


    private final JdbcTemplate
            jdbcTemplate;


    private final EmployeeRepository
            employeeRepository;


    public CurrentEmployeeService(

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
     * دریافت Employee از Authentication
     *
     * این متد برای EmployeePortalController
     * و بخش‌هایی که Authentication دارند
     * استفاده می‌شود.
     * ==========================================
     */

    @Transactional(readOnly = true)
    public Employee getCurrentEmployee(
            Authentication authentication
    ) {


        if (
                authentication == null
                        ||
                        !authentication.isAuthenticated()
                        ||
                        authentication.getName() == null
                        ||
                        authentication.getName().isBlank()
        ) {


            throw new IllegalStateException(
                    "کاربر وارد سامانه نشده است."
            );
        }


        return requireEmployee(
                authentication.getName()
        );
    }


    /*
     * ==========================================
     * دریافت Employee از Username
     *
     * این متد برای سرویس‌هایی مثل:
     *
     * QuarterlyPerformanceService
     * Manager performance
     * و سایر سرویس‌های Backend
     *
     * استفاده می‌شود.
     * ==========================================
     */

    @Transactional(readOnly = true)
    public Employee requireEmployee(
            String username
    ) {


        if (
                username == null
                        ||
                        username.isBlank()
        ) {


            throw new IllegalArgumentException(
                    "نام کاربری معتبر نیست."
            );
        }


        Long employeeId;


        try {


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

                            username.trim()
                    );


        } catch (
                EmptyResultDataAccessException exception
        ) {


            throw new IllegalStateException(
                    "برای حساب کاربری فعلی، کارمند مرتبطی تعریف نشده است."
            );
        }


        if (employeeId == null) {


            throw new IllegalStateException(
                    "حساب کاربری به کارمند متصل نشده است."
            );
        }


        Employee employee =
                employeeRepository
                        .findById(
                                employeeId
                        )
                        .orElseThrow(() ->

                                new IllegalStateException(
                                        "اطلاعات کارمند مرتبط با این حساب پیدا نشد."
                                )
                        );


        if (!employee.isActive()) {


            throw new IllegalStateException(
                    "کارمند مرتبط با این حساب غیرفعال است."
            );
        }


        /*
         * Department در Entity احتمالاً LAZY است.
         *
         * اینجا داخل Transaction مقداردهی می‌کنیم
         * چون QuarterlyPerformanceService
         * بعداً به department نیاز دارد.
         */

        if (
                employee.getDepartment()
                        != null
        ) {


            employee
                    .getDepartment()
                    .getId();


            employee
                    .getDepartment()
                    .getName();
        }


        return employee;
    }
}