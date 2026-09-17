package ir.ac.sutech.attendance_system.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @GetMapping("/dashboard")
    public String redirectToDashboard(
            Authentication authentication
    ) {

        if (hasRole(authentication, "ROLE_ADMIN")) {
            return "redirect:/admin/dashboard";
        }

        if (hasRole(authentication, "ROLE_OPERATOR")) {
            return "redirect:/operator/dashboard";
        }

        if (hasRole(authentication, "ROLE_PRESIDENT")) {
            return "redirect:/president/dashboard";
        }

        if (hasRole(authentication, "ROLE_MANAGER")) {
            return "redirect:/manager/dashboard";
        }

        return "redirect:/employee/dashboard";
    }

    private boolean hasRole(
            Authentication authentication,
            String role
    ) {

        if (authentication == null) {
            return false;
        }

        return authentication
                .getAuthorities()
                .stream()
                .anyMatch(authority ->
                        role.equals(
                                authority.getAuthority()
                        )
                );
    }
}
