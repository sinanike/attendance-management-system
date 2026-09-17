package ir.ac.sutech.attendance_system.service;

import ir.ac.sutech.attendance_system.model.UserAccount;
import ir.ac.sutech.attendance_system.repository.UserAccountRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DatabaseUserDetailsService
        implements UserDetailsService {

    private final UserAccountRepository userAccountRepository;

    public DatabaseUserDetailsService(
            UserAccountRepository userAccountRepository
    ) {
        this.userAccountRepository = userAccountRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        String normalizedUsername = username == null
                ? ""
                : username.trim();

        UserAccount account = userAccountRepository
                .findByUsernameIgnoreCase(normalizedUsername)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "نام کاربری یا رمز عبور نادرست است"
                ));

        return User.withUsername(account.getUsername())
                .password(account.getPasswordHash())
                .authorities("ROLE_" + account.getRole().name())
                .disabled(!account.canLogin())
                .build();
    }
}