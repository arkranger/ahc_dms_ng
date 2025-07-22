package ahc.dms.dao.dms.repositories;

import ahc.dms.dao.dms.entities.ObjectMaster;
import ahc.dms.dao.dms.entities.ObjectRole;
import ahc.dms.dao.dms.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ObjectRoleRepository extends JpaRepository<ObjectRole, Long> {

    boolean existsByObjectMasterAndRole(ObjectMaster objectMaster, Role role);

    Optional<ObjectRole> findByObjectMasterAndRole(ObjectMaster om, Role role);

    Optional<ObjectRole> findByObjectMasterAndRoleAndStatusTrue(ObjectMaster existingOm, Role role);
}
