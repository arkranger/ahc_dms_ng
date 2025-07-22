package ahc.dms.auth;


import ahc.dms.config.AppConstants;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private JwtAuthHelper jwtAuthHelper;
    private final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);
    private static final PathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request)
    {
        boolean match = AppConstants.JWT_IGNORED_URLS.stream()
                .anyMatch(pattern -> PATH_MATCHER.match(pattern, request.getRequestURI()));
        logger.info("Match for {} in JWT_IGNORED_URLS found : {}", request.getRequestURI(), match);
        return match;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        logger.info("Running doFilterInternal (JwtAuthFilter)");

        String authHeader = request.getHeader("Authorization");
        logger.info("Auth Header : {}", authHeader);

        String username = null;
        String token = null;
        String tokenType = null;

        //getting username from token
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            logger.info("Extracting Token");
            token = authHeader.substring(7);
            tokenType = this.jwtAuthHelper.getTokenTypeFromToken(token);
            logger.info("Token type : {}", tokenType);
            try {
                username = this.jwtAuthHelper.getUsernameFromToken(token);
            } catch (IllegalArgumentException e) {
                logger.info("Unable to get user");
            } catch (ExpiredJwtException e) {
                logger.info("JWT expired");
            } catch (MalformedJwtException e) {
                logger.info("Malformed JWT");
            }
        } else {
            logger.info("JWT doesn't start with Bearer");
        }
        logger.info("Username = {}", username);

        //validating token
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
            if (this.jwtAuthHelper.validateToken(token, tokenType, userDetails)) {
                //now set the authentication
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
                logger.info("Authentication Object : {}", SecurityContextHolder.getContext().getAuthentication());
            } else {
                logger.info("Invalid JWT");
            }
        } else {
            logger.info("User/Context is null");
        }
        filterChain.doFilter(request, response);
    }
}
