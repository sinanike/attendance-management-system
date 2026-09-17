package ir.ac.sutech.attendance_system.config;

import ir.ac.sutech.attendance_system.model.Role;
import ir.ac.sutech.attendance_system.model.UserAccount;
import ir.ac.sutech.attendance_system.repository.UserAccountRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class InitialAdminInitializer implements CommandLineRunner {

    private static final String INITIAL_USERNAME = "admin";
    private static final String INITIAL_PASSWORD = "admin123";

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    public InitialAdminInitializer(
            UserAccountRepository userAccountRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userAccountRepository.count() > 0) {
            return;
        }

        UserAccount initialAdmin = new UserAccount(
                INITIAL_USERNAME,
                passwordEncoder.encode(INITIAL_PASSWORD),
                Role.ADMIN
        );

        initialAdmin.setActive(true);
        userAccountRepository.save(initialAdmin);

        System.out.println(
                "Initial administrator account created: "
                        + INITIAL_USERNAME
        );
    }
}