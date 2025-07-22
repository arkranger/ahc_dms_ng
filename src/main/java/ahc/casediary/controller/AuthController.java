package ahc.casediary.controller;

import ahc.casediary.config.AppConstants;
import ahc.casediary.dao.services.RequestLogService;
import ahc.casediary.dao.services.RoleService;
import ahc.casediary.dao.services.TokenLogService;
import ahc.casediary.dao.services.UserService;
import ahc.casediary.payload.dto.ResetPasswordDto;
import ahc.casediary.payload.dto.RoleDto;
import ahc.casediary.payload.dto.TokenLogDto;
import ahc.casediary.payload.dto.UserDto;
import ahc.casediary.payload.request.JwtAuthRequest;
import ahc.casediary.payload.response.GenericResponse;
import ahc.casediary.payload.response.JwtAuthResponse;
import ahc.casediary.auth.JwtAuthHelper;
import ahc.casediary.utils.ResponseUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private JwtAuthHelper jwtAuthHelper;
    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private TokenLogService tokenLogService;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private UserService userService;
    @Autowired
    private RoleService roleService;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private RequestLogService requestLogService;
    private final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @PostMapping("/login")
    public ResponseEntity<GenericResponse<JwtAuthResponse>> loginUsingPassword(
            HttpServletRequest httpRequest,
            @RequestBody JwtAuthRequest jwtAuthRequest
    ) {
        requestLogService.logRequest(httpRequest);
        try {
            // 1. Authenticate and get the full Authentication object
            Authentication authentication = this.authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(
                            jwtAuthRequest.getUsername(),
                            jwtAuthRequest.getPassword()
                    ));
            // 2. MANUALLY set the security context (critical for stateless apps)
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (Exception ex) {
            if (ex instanceof UsernameNotFoundException) {
                return ResponseEntity.ok(ResponseUtil.error("User not found"));
            }
            else if (ex instanceof BadCredentialsException) {
                return ResponseEntity.ok(ResponseUtil.error("Incorrect password"));
            }
            else {
                return ResponseEntity.ok(ResponseUtil.error("Authentication failed"));
            }
        }
        //returns anonymousUser since session creation policy is stateless
        UserDetails userDetails = this.userDetailsService.loadUserByUsername(jwtAuthRequest.getUsername());
        //get only active roles, which are set in user entity
        Set<RoleDto> activeRoleSet = roleService.getActiveRoles(userDetails);
        UserDto authUserDto = modelMapper.map(userDetails, UserDto.class);
        authUserDto.setUserRoles(null);
        authUserDto.setRoles(activeRoleSet);


        String token = this.jwtAuthHelper.generateToken(userDetails, AppConstants.LOGIN_TOKEN);

        JwtAuthResponse jwtAuthResponse = new JwtAuthResponse();
        jwtAuthResponse.setToken(token);
        jwtAuthResponse.setMessage(AppConstants.JWT_CREATED);
        jwtAuthResponse.setUser(authUserDto);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        logger.info("Auth Authorities : {}", auth.getAuthorities());
        logger.info("User Authorities : {}", userDetails.getAuthorities());
        return ResponseEntity.ok(ResponseUtil.success(jwtAuthResponse,AppConstants.JWT_CREATED));

    }


    @GetMapping("/logout")
    public ResponseEntity<GenericResponse<JwtAuthResponse>> logoutUser(HttpServletRequest request) {

        requestLogService.logRequest(request);
        String authHeader = request.getHeader("Authorization");
        String token = authHeader.substring(7);
        String username = this.jwtAuthHelper.getUsernameFromToken(token);
        String tokenType = this.jwtAuthHelper.getTokenTypeFromToken(token);
        TokenLogDto tokenLogDto = tokenLogService.getToken(token, tokenType, username);
        TokenLogDto revokedToken = tokenLogService.revokeToken(tokenLogDto.getTokenId());

        JwtAuthResponse jwtAuthResponse = new JwtAuthResponse();
        jwtAuthResponse.setToken(revokedToken.getJwToken());
        jwtAuthResponse.setMessage(AppConstants.JWT_REVOKED);

        return ResponseEntity.ok(ResponseUtil.success(jwtAuthResponse, "Logged out successfully"));
    }

    //@PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/change-password/reset")
    public ResponseEntity<GenericResponse<?>> resetPassword(
            HttpServletRequest httpRequest,
            @RequestBody ResetPasswordDto passDto
    ) {
        requestLogService.logRequest(httpRequest);
        try {
            this.authenticationManager
                    .authenticate(new UsernamePasswordAuthenticationToken(
                            passDto.getUsername(),
                            passDto.getOldPassword()
                    ));
        } catch (Exception ex) {
            if (ex instanceof UsernameNotFoundException) {
                return ResponseEntity.ok(ResponseUtil.error("User not found"));
            }
            else if (ex instanceof BadCredentialsException) {
                return ResponseEntity.ok(ResponseUtil.error("Incorrect password"));
            }
            else {
                return ResponseEntity.ok(ResponseUtil.error("Authentication failed"));
            }
        }
        UserDto updatedUser = userService.changePassword(passDto.getUsername(), passDto.getNewPassword());
        return ResponseEntity.ok(ResponseUtil.success(null, "Password has been reset"));
    }

    @PostMapping("/change-password/forgot")
    public ResponseEntity<GenericResponse<?>> forgotPassword(
            HttpServletRequest httpRequest,
            @RequestBody UserDto userDto
    ) {
        requestLogService.logRequest(httpRequest);
        UserDto updatedUser = userService.changePassword(userDto.getUsername(), userDto.getPassword());
        return ResponseEntity.ok(ResponseUtil.success(null, "Password has been reset"));
    }

}
