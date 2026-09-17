package ir.ac.sutech.attendance_system.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Controller
public class AdminDashboardController {

    /*
     * React production entry point.
     *
     * Build output:
     * src/main/resources/static/admin/react/index.html
     *
     * We return the built index directly instead of using "forward:".
     */
    @GetMapping(
            value = "/admin/dashboard",
            produces = MediaType.TEXT_HTML_VALUE
    )
    @ResponseBody
    public ResponseEntity<String> showAdminDashboard()
            throws IOException {

        ClassPathResource index =
                new ClassPathResource(
                        "static/admin/react/index.html"
                );

        String html =
                new String(
                        index.getInputStream().readAllBytes(),
                        StandardCharsets.UTF_8
                );

        return ResponseEntity
                .ok()
                .contentType(MediaType.TEXT_HTML)
                .cacheControl(CacheControl.noStore())
                .body(html);
    }
}
