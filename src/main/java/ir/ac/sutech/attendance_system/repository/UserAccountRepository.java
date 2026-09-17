package ir.ac.sutech.attendance_system.repository;

import ir.ac.sutech.attendance_system.model.Role;
import ir.ac.sutech.attendance_system.model.UserAccount;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserAccountRepository
        extends JpaRepository<UserAccount, Long> {

    Optional<UserAccount> findByUsernameIgnoreCase(
            String username
    );

    boolean existsByUsernameIgnoreCase(
            String username
    );

    boolean existsByUsernameIgnoreCaseAndIdNot(
            String username,
            Long id
    );

    boolean existsByEmployeeId(
            Long employeeId
    );

    boolean existsByEmployeeIdAndIdNot(
            Long employeeId,
            Long id
    );

    long countByRoleAndActiveTrue(
            Role role
    );

    @EntityGraph(attributePaths = "employee")
    List<UserAccount> findAllByOrderByUsernameAsc();
}