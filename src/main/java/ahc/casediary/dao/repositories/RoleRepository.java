package ahc.casediary.dao.repositories;

import ahc.casediary.dao.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByRoleId(Integer roleId);
    Optional<Role> findByRoleName(String roleName);
}
