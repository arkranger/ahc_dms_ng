package ahc.casediary.config;

import java.util.Set;

public class AppConstants {

    // Ignore urls which are accessible to public
    public static final String[] PUBLIC_URLS = {
            "/api/auth/login",
            "/api/auth/register",
            "/actuator/**"
    };

    public static final String[] WEB_IGNORES = {
            "/swagger-ui/**",
            "/v3/api-docs*/**",
            "/resources/static/**"
    };


    /*
        Ant-style wildcards:
            ? - matches one character
            * - matches zero or more characters within a path segment
            ** - matches zero or more path segments
            {string} - matches a path segment and captures it as a variable
     */
    // Ignore urls which do not require jwt-based authentication
    public static final Set<String> JWT_IGNORED_URLS = Set.of(
            "/api/auth/login",
            "/api/auth/register",
            "/actuator/**"
    );

    // Ignore urls which do not require role-based permissions (ie performed by everyone)
    public static final Set<String> REQUEST_AUTH_IGNORED_URLS = Set.of(
            "/api/auth/login",
            "/api/auth/register",
            "/api/auth/forgot-password",
            "/api/auth/reset-password",
            "/api/object/**",
            "/actuator/**"
    );

    public static final String PAGE_NUMBER = "0";
    public static final String PAGE_SIZE = "5";
    public static final String SORT_DIR = "asc";
    public static final String SORT_ROLE_BY = "roleId";
    public static final String SORT_USER_BY = "userId";
    public static final String SORT_OBJECT_BY = "omId";

    public static final String JWT_SECRET = "7c2700653ec7ecf51345e95f2f0f2d8322e2cc2147b6f9442e9a5823e0e263eab27bfb8f0e99e83a1b8f19f0";
    public static final long JWT_TOKEN_VALIDITY = 36000000;
    public static final String JWT_CREATED = "Created";
    public static final String JWT_REVOKED = "Revoked";

    public static final String LOGIN_TOKEN = "Login";
    public static final String FORGOT_TOKEN = "Forgot";

}
