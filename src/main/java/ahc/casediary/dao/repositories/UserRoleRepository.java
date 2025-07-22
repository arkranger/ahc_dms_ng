package ahc.casediary.dao.repositories;

import ahc.casediary.dao.entities.Role;
import ahc.casediary.dao.entities.User;
import ahc.casediary.dao.entities.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

    // Find by user and role
    Optional<UserRole> findByUserAndRole(User user, Role role);
    // Find all active roles for a user
    List<UserRole> findByUserAndStatusTrue(User user);
    // find active user-role mapping
    boolean existsByUserAndRoleAndStatusTrue(User user, Role role);
    // find by roles of a user
    Set<UserRole> findByUser(User user);
    // if user-role mapping exists
    boolean existsByUserAndRole(User user, Role role);
}