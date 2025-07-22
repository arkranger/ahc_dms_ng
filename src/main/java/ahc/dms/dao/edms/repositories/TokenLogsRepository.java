package ahc.dms.dao.edms.repositories;

import ahc.dms.dao.edms.entities.TokenLogs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TokenLogsRepository extends JpaRepository<TokenLogs, Long> {

    TokenLogs findByUsername(String username);
    TokenLogs findByUsernameAndJwtToken(String username, String token);

}
