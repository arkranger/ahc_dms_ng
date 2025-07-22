package ahc.dms.security;

import ahc.dms.config.AppConstants;
import ahc.dms.dao.dms.entities.ObjectMaster;
import ahc.dms.dao.dms.entities.ObjectRole;
import ahc.dms.dao.dms.repositories.ObjectMasterRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

@Component
public class RequestAuthFilter extends OncePerRequestFilter {

    @Autowired
    private ObjectMasterRepository objectMasterRepository;
    private final Logger logger = LoggerFactory.getLogger(RequestAuthFilter.class);
    private static final PathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {

        logger.info("Checking REQUEST_AUTH_IGNORED_URLS for : {}", request.getRequestURI());
        return AppConstants.REQUEST_AUTH_IGNORED_URLS.stream()
                .anyMatch(pattern -> PATH_MATCHER.match(pattern, request.getRequestURI()));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        logger.info("Running doFilterInternal (RequestAuthFilter)");

        String uri = request.getRequestURI();
        String method = request.getMethod();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // If not authenticated yet, let the Spring Security handle it
        if (auth == null || !auth.isAuthenticated()) {
            logger.info("user is not yet authenticated????");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
            return;
        }

        // Get user roles
        String authRole = auth.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse(null);
        logger.info("Auth Role : {}", authRole);

        Optional<ObjectMaster> objectMaster = objectMasterRepository
                .findBestMatchingPrefix(uri, method);

        if (objectMaster.isPresent()) {
            logger.info("Object master entity found in database : {}", objectMaster.get().getRequestUri());
            Set<ObjectRole> objectRoles = objectMaster.get().getObjectRoles();
            boolean roleMatched = objectRoles
                    .stream()
                    .anyMatch(objectRole -> {
                        String roleName = objectRole.getRole().getRoleName();
                        Boolean roleStatus = objectRole.getRole().getStatus();
                        Boolean objectRoleStatus = objectRole.getStatus();

                        return Boolean.TRUE.equals(roleStatus) &&
                                Boolean.TRUE.equals(objectRoleStatus) &&
                                roleName.equalsIgnoreCase(authRole);
                    });

            if (roleMatched) {
                logger.info("Auth Role matched");
                filterChain.doFilter(request, response);
            } else {
                logger.info("Access Denied (Mapping absent or disabled)");
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
            }
        } else {
            logger.info("API Not Registered : {}, With Method : {}", uri, method);
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "API Not Registered");
        }
        //filterChain.doFilter(request, response);
    }
}
