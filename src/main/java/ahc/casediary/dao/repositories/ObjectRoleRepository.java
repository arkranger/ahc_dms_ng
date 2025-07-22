package ahc.casediary.dao.repositories;

import ahc.casediary.dao.entities.ObjectMaster;
import ahc.casediary.dao.entities.ObjectRole;
import ahc.casediary.dao.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ObjectRoleRepository extends JpaRepository<ObjectRole, Long> {

    boolean existsByObjectMasterAndRole(ObjectMaster objectMaster, Role role);

    Optional<ObjectRole> findByObjectMasterAndRole(ObjectMaster om, Role role);

    Optional<ObjectRole> findByObjectMasterAndRoleAndStatusTrue(ObjectMaster existingOm, Role role);
}
