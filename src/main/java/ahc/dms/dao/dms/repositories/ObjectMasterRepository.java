package ahc.dms.dao.dms.repositories;

import ahc.dms.dao.dms.entities.ObjectMaster;
import org.modelmapper.ModelMapper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ObjectMasterRepository extends JpaRepository<ObjectMaster, Long> {
    Optional<ObjectMaster> findByRequestUriAndRequestMethodAndStatusTrue(String uri, String method);
    Optional<ObjectMaster> findByRequestUriStartingWithAndRequestMethodAndStatusTrue(String uri, String method);

    @Query("SELECT om FROM ObjectMaster om " +
            "WHERE (:uri = om.requestUri OR :uri LIKE CONCAT(om.requestUri, '/%')) " +
            "AND om.requestMethod = :method AND om.status = true " +
            "ORDER BY LENGTH(om.requestUri) DESC")
    Optional<ObjectMaster> findBestMatchingPrefix(@Param("uri") String uri, @Param("method") String method);

    Optional<ObjectMaster> findByRequestUriAndRequestMethod(String requestUri, String requestMethod);
}