package ir.ac.sutech.attendance_system.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                .authorizeHttpRequests(auth -> auth

                        /*
                         * مسیرهای عمومی و فایل‌های استاتیک
                         */
                        .requestMatchers(
                                "/",
                                "/login",
                                "/error",
                                "/css/**",
                                "/js/**",
                                "/images/**"
                        )
                        .permitAll()

                        /*
                         * پنل اپراتور
                         *
                         * ADMIN نیز برای تست/پشتیبانی
                         * می‌تواند این صفحات را باز کند.
                         */
                        .requestMatchers("/operator/**")
                        .hasAnyRole(
                                "ADMIN",
                                "OPERATOR"
                        )

                        /*
                         * پنل ریاست دانشگاه
                         *
                         * پنل President فقط گزارش و KPI است.
                         */
                        .requestMatchers("/president/**")
                        .hasAnyRole(
                                "ADMIN",
                                "PRESIDENT"
                        )

                        /*
                         * مدیریت حساب‌های Admin
                         * فقط برای مدیر امور اداری.
                         *
                         * اپراتور مدیریت کاربران اختصاصی خودش
                         * را در /operator/users دارد.
                         */
                        .requestMatchers("/admin/users/**")
                        .hasRole("ADMIN")

                        /*
                         * تمام بخش‌های Admin
                         */
                        .requestMatchers("/admin/**")
                        .hasRole("ADMIN")

                        /*
                         * مدیر بخش
                         */
                        .requestMatchers("/manager/**")
                        .hasAnyRole(
                                "ADMIN",
                                "MANAGER"
                        )

                        /*
                         * پنل شخصی کارمند.
                         *
                         * Manager نیز چون خودش یک کارمند است،
                         * می‌تواند درخواست شخصی ثبت کند.
                         */
                        .requestMatchers("/employee/**")
                        .hasAnyRole(
                                "ADMIN",
                                "MANAGER",
                                "EMPLOYEE"
                        )

                        /*
                         * سایر مسیرها فقط بعد از Login
                         */
                        .anyRequest()
                        .authenticated()
                )

                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl(
                                "/dashboard",
                                true
                        )
                        .failureUrl("/login?error")
                        .permitAll()
                )

                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }
}
