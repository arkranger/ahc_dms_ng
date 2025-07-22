package ahc.dms.dao.dms.repositories;

import ahc.dms.dao.dms.entities.TokenLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TokenLogRepository extends JpaRepository<TokenLog, Long> {

    TokenLog findByUsername(String username);
    TokenLog findByUsernameAndJwToken(String username, String token);
    TokenLog findByUsernameAndTokenType(String username, String tokenType);
    TokenLog findByUsernameAndJwTokenAndTokenType(String username, String token, String tokenType);
}
