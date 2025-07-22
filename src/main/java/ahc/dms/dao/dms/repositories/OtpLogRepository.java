package ahc.dms.dao.dms.repositories;

import ahc.dms.dao.dms.entities.OtpLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpLogRepository extends JpaRepository<OtpLog, Long> {
    Optional<OtpLog> findByUsernameAndOtpType(String username, String otpType);
    Optional<OtpLog> findByUsernameAndOtpTypeAndOtpValueAndOtpStatusTrue(String username, String otpTypeLogin, String otp);
}
