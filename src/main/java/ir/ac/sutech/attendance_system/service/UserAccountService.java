package ir.ac.sutech.attendance_system.service;

import ir.ac.sutech.attendance_system.dto.UserAccountForm;
import ir.ac.sutech.attendance_system.model.Employee;
import ir.ac.sutech.attendance_system.model.Role;
import ir.ac.sutech.attendance_system.model.UserAccount;
import ir.ac.sutech.attendance_system.repository.EmployeeRepository;
import ir.ac.sutech.attendance_system.repository.UserAccountRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class UserAccountService {

    private static final int MINIMUM_PASSWORD_LENGTH = 8;

    private final UserAccountRepository
            userAccountRepository;

    private final EmployeeRepository
            employeeRepository;

    private final PasswordEncoder
            passwordEncoder;

    public UserAccountService(
            UserAccountRepository userAccountRepository,
            EmployeeRepository employeeRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userAccountRepository =
                userAccountRepository;

        this.employeeRepository =
                employeeRepository;

        this.passwordEncoder =
                passwordEncoder;
    }

    /*
     * ==========================================
     * عملیات عمومی Admin
     * ==========================================
     */

    public List<UserAccount> findAllAccounts() {

        return userAccountRepository
                .findAllByOrderByUsernameAsc();
    }

    public long countAccounts() {

        return userAccountRepository.count();
    }

    public List<Employee> findActiveEmployees() {

        return employeeRepository
                .findByActiveTrueOrderByLastNameAscFirstNameAsc();
    }

    public UserAccountForm findFormById(
            Long accountId
    ) {

        UserAccount account =
                findAccountOrThrow(accountId);

        return toForm(account);
    }

    @Transactional
    public UserAccount createAccount(
            UserAccountForm form
    ) {

        String username =
                normalizeUsername(
                        form.getUsername()
                );

        if (
                userAccountRepository
                        .existsByUsernameIgnoreCase(
                                username
                        )
        ) {
            throw new IllegalArgumentException(
                    "این نام کاربری قبلاً ثبت شده است"
            );
        }

        validatePassword(
                form.getPassword(),
                true
        );

        Role role =
                requireRole(
                        form.getRole()
                );

        Employee employee =
                resolveEmployee(
                        role,
                        form.getEmployeeId(),
                        null
                );

        UserAccount account =
                new UserAccount(
                        username,
                        passwordEncoder.encode(
                                form.getPassword()
                        ),
                        role
                );

        account.setEmployee(employee);
        account.setActive(form.isActive());

        return userAccountRepository.save(account);
    }

    @Transactional
    public UserAccount updateAccount(
            Long accountId,
            UserAccountForm form,
            String currentUsername
    ) {

        UserAccount account =
                findAccountOrThrow(accountId);

        String username =
                normalizeUsername(
                        form.getUsername()
                );

        if (
                userAccountRepository
                        .existsByUsernameIgnoreCaseAndIdNot(
                                username,
                                accountId
                        )
        ) {
            throw new IllegalArgumentException(
                    "این نام کاربری متعلق به حساب دیگری است"
            );
        }

        Role newRole =
                requireRole(
                        form.getRole()
                );

        boolean editingOwnAccount =
                account.getUsername()
                        .equalsIgnoreCase(
                                currentUsername
                        );

        if (
                editingOwnAccount
                        &&
                        (
                                newRole != account.getRole()
                                        ||
                                        !form.isActive()
                        )
        ) {
            throw new IllegalArgumentException(
                    "نمی‌توانید نقش یا وضعیت حسابی را که با آن وارد شده‌اید تغییر دهید"
            );
        }

        protectLastAdministrator(
                account,
                newRole,
                form.isActive()
        );

        Employee employee =
                resolveEmployee(
                        newRole,
                        form.getEmployeeId(),
                        accountId
                );

        if (
                form.getPassword() != null
                        &&
                        !form.getPassword().isBlank()
        ) {

            validatePassword(
                    form.getPassword(),
                    false
            );

            account.setPasswordHash(
                    passwordEncoder.encode(
                            form.getPassword()
                    )
            );
        }

        account.setUsername(username);
        account.setRole(newRole);
        account.setEmployee(employee);
        account.setActive(form.isActive());
        account.markUpdated();

        return userAccountRepository.save(account);
    }

    @Transactional
    public UserAccount updateStatus(
            Long accountId,
            boolean active,
            String currentUsername
    ) {

        UserAccount account =
                findAccountOrThrow(accountId);

        if (
                account.getUsername()
                        .equalsIgnoreCase(
                                currentUsername
                        )
                        &&
                        !active
        ) {
            throw new IllegalArgumentException(
                    "نمی‌توانید حسابی را که با آن وارد شده‌اید غیرفعال کنید"
            );
        }

        protectLastAdministrator(
                account,
                account.getRole(),
                active
        );

        account.setActive(active);
        account.markUpdated();

        return userAccountRepository.save(account);
    }

    public UserAccount findByUsername(
            String username
    ) {

        return userAccountRepository
                .findByUsernameIgnoreCase(
                        username
                )
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "حساب کاربری پیدا نشد"
                        )
                );
    }

    /*
     * ==========================================
     * عملیات امن مخصوص OPERATOR
     *
     * اپراتور طبق Product Backlog مدیریت کاربران
     * و سطح دسترسی را دارد؛ با این حال برای جلوگیری
     * از Privilege Escalation اجازه ساخت/ویرایش
     * حساب ADMIN را ندارد.
     * ==========================================
     */

    public List<UserAccount>
    findOperatorManageableAccounts() {

        return userAccountRepository
                .findAllByOrderByUsernameAsc()
                .stream()
                .filter(account ->
                        account.getRole() != Role.ADMIN
                )
                .toList();
    }

    public long countOperatorManageableAccounts() {

        return findOperatorManageableAccounts()
                .size();
    }

    public UserAccountForm
    findOperatorFormById(
            Long accountId
    ) {

        UserAccount account =
                findAccountOrThrow(accountId);

        ensureOperatorCanManage(
                account
        );

        return toForm(account);
    }

    public List<Role>
    findOperatorAssignableRoles() {

        return Arrays.stream(
                        Role.values()
                )
                .filter(role ->
                        role != Role.ADMIN
                )
                .toList();
    }

    @Transactional
    public UserAccount
    createAccountByOperator(
            UserAccountForm form
    ) {

        ensureOperatorAssignableRole(
                form.getRole()
        );

        return createAccount(form);
    }

    @Transactional
    public UserAccount
    updateAccountByOperator(
            Long accountId,
            UserAccountForm form,
            String currentUsername
    ) {

        UserAccount current =
                findAccountOrThrow(
                        accountId
                );

        ensureOperatorCanManage(
                current
        );

        ensureOperatorAssignableRole(
                form.getRole()
        );

        return updateAccount(
                accountId,
                form,
                currentUsername
        );
    }

    @Transactional
    public UserAccount
    updateStatusByOperator(
            Long accountId,
            boolean active,
            String currentUsername
    ) {

        UserAccount account =
                findAccountOrThrow(
                        accountId
                );

        ensureOperatorCanManage(
                account
        );

        return updateStatus(
                accountId,
                active,
                currentUsername
        );
    }

    /*
     * ==========================================
     * Helpers
     * ==========================================
     */

    private UserAccountForm toForm(
            UserAccount account
    ) {

        UserAccountForm form =
                new UserAccountForm();

        form.setUsername(
                account.getUsername()
        );

        form.setRole(
                account.getRole()
        );

        form.setActive(
                account.isActive()
        );

        if (
                account.getEmployee() != null
        ) {

            form.setEmployeeId(
                    account
                            .getEmployee()
                            .getId()
            );
        }

        return form;
    }

    private UserAccount findAccountOrThrow(
            Long accountId
    ) {

        return userAccountRepository
                .findById(accountId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "حساب کاربری موردنظر پیدا نشد"
                        )
                );
    }

    private Employee resolveEmployee(
            Role role,
            Long employeeId,
            Long currentAccountId
    ) {

        if (role == null) {
            throw new IllegalArgumentException(
                    "سطح دسترسی را انتخاب کنید"
            );
        }

        /*
         * این سه Role الزاماً Employee نیستند.
         */
        boolean employeeOptional =
                role == Role.ADMIN
                        ||
                        role == Role.OPERATOR
                        ||
                        role == Role.PRESIDENT;

        if (employeeId == null) {

            if (employeeOptional) {
                return null;
            }

            throw new IllegalArgumentException(
                    "برای مدیر بخش و کارمند باید یک کارمند انتخاب شود"
            );
        }

        boolean alreadyLinked =
                currentAccountId == null

                        ? userAccountRepository
                        .existsByEmployeeId(
                                employeeId
                        )

                        : userAccountRepository
                        .existsByEmployeeIdAndIdNot(
                                employeeId,
                                currentAccountId
                        );

        if (alreadyLinked) {
            throw new IllegalArgumentException(
                    "برای این کارمند قبلاً حساب کاربری ساخته شده است"
            );
        }

        return employeeRepository
                .findById(employeeId)
                .filter(Employee::isActive)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "کارمند انتخاب‌شده معتبر یا فعال نیست"
                        )
                );
    }

    private void validatePassword(
            String password,
            boolean required
    ) {

        if (
                password == null
                        ||
                        password.isBlank()
        ) {

            if (required) {
                throw new IllegalArgumentException(
                        "رمز عبور را وارد کنید"
                );
            }

            return;
        }

        if (
                password.length()
                        <
                        MINIMUM_PASSWORD_LENGTH
        ) {
            throw new IllegalArgumentException(
                    "رمز عبور باید حداقل ۸ کاراکتر باشد"
            );
        }

        if (password.length() > 72) {
            throw new IllegalArgumentException(
                    "رمز عبور نمی‌تواند بیشتر از ۷۲ کاراکتر باشد"
            );
        }
    }

    private void protectLastAdministrator(
            UserAccount account,
            Role newRole,
            boolean newActive
    ) {

        boolean removesActiveAdmin =
                account.getRole() == Role.ADMIN
                        &&
                        account.isActive()
                        &&
                        (
                                newRole != Role.ADMIN
                                        ||
                                        !newActive
                        );

        if (
                removesActiveAdmin
                        &&
                        userAccountRepository
                                .countByRoleAndActiveTrue(
                                        Role.ADMIN
                                )
                                <= 1
        ) {
            throw new IllegalArgumentException(
                    "آخرین حساب مدیر فعال را نمی‌توان غیرفعال کرد یا نقش آن را تغییر داد"
            );
        }
    }

    private void ensureOperatorCanManage(
            UserAccount account
    ) {

        if (
                account.getRole() == Role.ADMIN
        ) {
            throw new IllegalArgumentException(
                    "اپراتور اجازه ویرایش حساب مدیر امور اداری را ندارد"
            );
        }
    }

    private void ensureOperatorAssignableRole(
            Role role
    ) {

        Role validRole =
                requireRole(role);

        if (
                validRole == Role.ADMIN
        ) {
            throw new IllegalArgumentException(
                    "اپراتور اجازه اختصاص نقش مدیر امور اداری را ندارد"
            );
        }
    }

    private Role requireRole(
            Role role
    ) {

        if (role == null) {
            throw new IllegalArgumentException(
                    "سطح دسترسی را انتخاب کنید"
            );
        }

        return role;
    }

    private String normalizeUsername(
            String username
    ) {

        if (
                username == null
                        ||
                        username.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "نام کاربری را وارد کنید"
            );
        }

        return username
                .trim()
                .toLowerCase(
                        Locale.ENGLISH
                );
    }
}
