package ir.ac.sutech.attendance_system.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class JalaliUiInjectionFilter extends OncePerRequestFilter {

    private static final String CSS_TAG =
            "<link rel=\"stylesheet\" href=\"/css/jalali-date.css?v=1\">";

    private static final String JS_TAG =
            "<script src=\"/js/jalali-date.js?v=1\" defer></script>";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        ContentCachingResponseWrapper wrapper =
                new ContentCachingResponseWrapper(response);

        filterChain.doFilter(request, wrapper);

        String contentType = wrapper.getContentType();

        if (contentType == null
                || !contentType.toLowerCase().contains("text/html")) {
            wrapper.copyBodyToResponse();
            return;
        }

        byte[] originalBody = wrapper.getContentAsByteArray();

        if (originalBody.length == 0) {
            wrapper.copyBodyToResponse();
            return;
        }

        Charset charset = resolveCharset(wrapper.getCharacterEncoding());
        String html = new String(originalBody, charset);

        if (!html.contains("/css/jalali-date.css")) {
            html = injectBeforeClosingTag(
                    html,
                    "</head>",
                    CSS_TAG
            );
        }

        if (!html.contains("/js/jalali-date.js")) {
            html = injectBeforeClosingTag(
                    html,
                    "</body>",
                    JS_TAG
            );
        }

        byte[] modifiedBody = html.getBytes(charset);

        wrapper.resetBuffer();
        wrapper.setContentLength(modifiedBody.length);
        wrapper.getOutputStream().write(modifiedBody);
        wrapper.copyBodyToResponse();
    }

    private String injectBeforeClosingTag(
            String html,
            String closingTag,
            String assetTag
    ) {
        int index = html.toLowerCase()
                .lastIndexOf(closingTag.toLowerCase());

        if (index < 0) {
            return html + assetTag;
        }

        return html.substring(0, index)
                + assetTag
                + html.substring(index);
    }

    private Charset resolveCharset(String characterEncoding) {
        if (characterEncoding == null || characterEncoding.isBlank()) {
            return StandardCharsets.UTF_8;
        }

        try {
            return Charset.forName(characterEncoding);
        } catch (RuntimeException exception) {
            return StandardCharsets.UTF_8;
        }
    }
}
