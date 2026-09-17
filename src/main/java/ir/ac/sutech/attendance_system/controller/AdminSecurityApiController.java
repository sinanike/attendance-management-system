package ir.ac.sutech.attendance_system.controller;

import ir.ac.sutech.attendance_system.dto.AdminCurrentUserProfileResponse;
import ir.ac.sutech.attendance_system.model.Employee;
import ir.ac.sutech.attendance_system.model.UserAccount;
import ir.ac.sutech.attendance_system.service.UserAccountService;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/admin/api/security")
public class AdminSecurityApiController {

    private final UserAccountService
            userAccountService;


    public AdminSecurityApiController(

            UserAccountService userAccountService

    ) {

        this.userAccountService =
                userAccountService;
    }


    /*
     * ==========================================
     * CSRF برای Logout React
     * ==========================================
     */

    @GetMapping("/csrf")
    public Map<String, String> getCsrfToken(
            CsrfToken csrfToken
    ) {

        return Map.of(
                "token",
                csrfToken.getToken(),

                "headerName",
                csrfToken.getHeaderName(),

                "parameterName",
                csrfToken.getParameterName()
        );
    }


    /*
     * ==========================================
     * مشخصات حساب Admin واردشده
     * ==========================================
     */

    @GetMapping("/profile")
    @Transactional(readOnly = true)
    public AdminCurrentUserProfileResponse
    getCurrentProfile(

            Authentication authentication

    ) {

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


        UserAccount account =
                userAccountService
                        .findByUsername(
                                authentication.getName()
                        );


        Employee employee =
                account.getEmployee();


        boolean employeeLinked =
                employee != null;


        String fullName =
                null;

        String personnelCode =
                null;

        String nationalCode =
                null;

        String department =
                null;


        if (employeeLinked) {

            String firstName =
                    employee.getFirstName() == null
                            ? ""
                            : employee.getFirstName().trim();

            String lastName =
                    employee.getLastName() == null
                            ? ""
                            : employee.getLastName().trim();


            fullName =
                    (
                            firstName
                                    +
                                    " "
                                    +
                                    lastName
                    )
                            .trim();


            personnelCode =
                    employee.getPersonnelCode();


            nationalCode =
                    employee.getNationalCode();


            if (
                    employee.getDepartment()
                            != null
            ) {

                department =
                        employee
                                .getDepartment()
                                .getName();
            }
        }


        return new AdminCurrentUserProfileResponse(

                account.getUsername(),

                account.getRole().name(),

                account.getRole().getDisplayName(),

                account.isActive(),

                employeeLinked,

                employeeLinked
                        ? employee.getId()
                        : null,

                fullName,

                personnelCode,

                nationalCode,

                department
        );
    }
}
