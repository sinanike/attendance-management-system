package ir.ac.sutech.attendance_system.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RoleBasedAuthenticationSuccessHandler
        implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        /*
         * مدیر امور اداری
         */
        if (hasRole(
                authentication,
                "ROLE_ADMIN"
        )) {

            response.sendRedirect(
                    "/admin/dashboard"
            );

            return;
        }


        /*
         * مدیر بخش
         */
        if (hasRole(
                authentication,
                "ROLE_MANAGER"
        )) {

            response.sendRedirect(
                    "/manager/dashboard"
            );

            return;
        }


        /*
         * کارمند
         */
        if (hasRole(
                authentication,
                "ROLE_EMPLOYEE"
        )) {

            response.sendRedirect(
                    "/employee/dashboard"
            );

            return;
        }


        /*
         * اگر نقش شناخته‌شده نبود
         */
        response.sendRedirect(
                "/"
        );
    }


    private boolean hasRole(
            Authentication authentication,
            String role
    ) {

        return authentication
                .getAuthorities()
                .stream()
                .map(
                        GrantedAuthority::getAuthority
                )
                .anyMatch(
                        role::equals
                );
    }
}