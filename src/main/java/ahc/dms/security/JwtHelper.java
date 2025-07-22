package ahc.dms.security;

import ahc.dms.config.AppConstants;
import ahc.dms.payload.dto.TokenLogDto;
import ahc.dms.dao.dms.services.TokenLogService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtHelper {

    @Autowired
    private TokenLogService tokenLogService;
    private SecretKey key;
    private final Logger logger = LoggerFactory.getLogger(JwtHelper.class);

    // Initializes the key after the class is instantiated and the jwtSecret is injected,
    // preventing the repeated creation of the key and enhancing performance
    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(AppConstants.JWT_SECRET.getBytes(StandardCharsets.UTF_8));
    }

    //GENERATE TOKEN FOR USER
    public String generateToken(UserDetails userDetails, String tokenType) {

        logger.info("user details object username = {}", userDetails.getUsername());
        Map<String, Object> claims = new HashMap<>();
        claims.put("tokenType", tokenType);
        Date expiration = new Date(System.currentTimeMillis() + AppConstants.JWT_TOKEN_VALIDITY);
        logger.info("Token will expire at {}", expiration);
        String token = Jwts.builder()
                .claims(claims)
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(expiration)
                .signWith(key)
                .compact();

        logger.info("token : {}", token);
        TokenLogDto existingTokenLogDto = tokenLogService.getTokenByUsernameAndTokenType(userDetails.getUsername(), tokenType);
        if (existingTokenLogDto !=null) {
            logger.info("renewing token!!!");
            existingTokenLogDto.setJwToken(token);
            existingTokenLogDto.setTokenStatus(true);
            tokenLogService.saveToken(existingTokenLogDto);
        } else {
            logger.info("creating new token!!!");
            TokenLogDto tokenLogDto = new TokenLogDto();
            tokenLogDto.setJwToken(token);
            tokenLogDto.setUsername(userDetails.getUsername());
            tokenLogDto.setExpirationDate(expiration);
            tokenLogDto.setTokenType(tokenType);
            tokenLogDto.setTokenStatus(true);
            tokenLogService.saveToken(tokenLogDto);
        }
        logger.info("returning token!!!!");
        return token;
    }

    //VALIDATE TOKEN
    public Boolean validateToken(String token, String tokenType, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        //check username and token expiry
        boolean validToken = (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        //if toke is valid
        if (validToken) {
            //check token status from db
            TokenLogDto existingToken = tokenLogService.getToken(token, tokenType, userDetails.getUsername());
            return (existingToken != null) && existingToken.getTokenStatus();
        }
        return false;
    }

    //GET ALL CLAIM FROM TOKEN
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    //GET SINGLE CLAIM FROM TOKEN
    private <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    //GET USERNAME FROM TOKEN
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    public String getTokenTypeFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.get("tokenType", String.class);
    }

    //CHECKS TOKEN EXPIRY
    private boolean isTokenExpired(String token) {
        final Date expiry = getClaimFromToken(token, Claims::getExpiration);
        return expiry.before(new Date());
    }

}
