package ahc.dms.controller;

import ahc.dms.config.AppConstants;
import ahc.dms.dao.dms.services.*;
import ahc.dms.payload.dto.*;
import ahc.dms.payload.request.JwtAuthRequest;
import ahc.dms.payload.response.ApiResponse;
import ahc.dms.payload.response.JwtAuthResponse;
import ahc.dms.security.JwtHelper;
import ahc.dms.utils.OtpHelper;
import ahc.dms.utils.ResponseUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.Set;

@RestController
@RequestMapping("/dms/auth")
public class AuthController {

    @Autowired
    private JwtHelper jwtHelper;
    @Autowired
    private OtpHelper otpHelper;
    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private TokenLogService tokenLogService;
    @Autowired
    private OtpLogService otpLogService;
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

    @PostMapping("/login-password")
    public ResponseEntity<ApiResponse<JwtAuthResponse>> loginUsingPassword(
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


        String token = this.jwtHelper.generateToken(userDetails, AppConstants.LOGIN_TOKEN);

        JwtAuthResponse jwtAuthResponse = new JwtAuthResponse();
        jwtAuthResponse.setToken(token);
        jwtAuthResponse.setMessage(AppConstants.JWT_CREATED);
        jwtAuthResponse.setUser(authUserDto);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        logger.info("Auth Authorities : {}", auth.getAuthorities());
        logger.info("User Authorities : {}", userDetails.getAuthorities());
        return ResponseEntity.ok(ResponseUtil.success(jwtAuthResponse,AppConstants.JWT_CREATED));

    }

    @PostMapping("/login-otp")
    public ResponseEntity<ApiResponse<JwtAuthResponse>> loginUsingOtp(
            HttpServletRequest httpRequest,
            @RequestBody JwtAuthRequest jwtAuthRequest
    ) {
        requestLogService.logRequest(httpRequest);
        logger.info("inside login-otp controller");

        boolean authStatus = otpLogService.verifyLoginOtp(jwtAuthRequest.getUsername(), jwtAuthRequest.getOtp());
        if (authStatus) {
            //returns anonymousUser since session creation policy is stateless
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(jwtAuthRequest.getUsername());
            String token = this.jwtHelper.generateToken(userDetails, AppConstants.LOGIN_TOKEN);

            JwtAuthResponse jwtAuthResponse = new JwtAuthResponse();
            jwtAuthResponse.setToken(token);
            jwtAuthResponse.setMessage(AppConstants.JWT_CREATED);
            return ResponseEntity.ok(ResponseUtil.success(jwtAuthResponse, "Logged-in successful"));
        }
        return ResponseEntity.ok(ResponseUtil.error("Invalid otp"));
    }

    @GetMapping("/logout")
    public ResponseEntity<ApiResponse<JwtAuthResponse>> logoutUser(HttpServletRequest request) {

        requestLogService.logRequest(request);
        String authHeader = request.getHeader("Authorization");
        String token = authHeader.substring(7);
        String username = this.jwtHelper.getUsernameFromToken(token);
        String tokenType = this.jwtHelper.getTokenTypeFromToken(token);
        TokenLogDto tokenLogDto = tokenLogService.getToken(token, tokenType, username);
        TokenLogDto revokedToken = tokenLogService.revokeToken(tokenLogDto.getTokenId());

        JwtAuthResponse jwtAuthResponse = new JwtAuthResponse();
        jwtAuthResponse.setToken(revokedToken.getJwToken());
        jwtAuthResponse.setMessage(AppConstants.JWT_REVOKED);

        return ResponseEntity.ok(ResponseUtil.success(jwtAuthResponse, "Logged out successfully"));
    }

    @PostMapping("/request-otp")
    public ResponseEntity<ApiResponse<OtpLogDto>> requestOtp(
            HttpServletRequest httpRequest,
            @RequestBody OtpLogDto requestOtp
    ) {
        requestLogService.logRequest(httpRequest);
        logger.info("otpDto : {}", requestOtp);
        String otp = String.valueOf(new Random().nextInt(9000) + 1000);
        UserDto savedUser = userService.getUserByUsername(requestOtp.getUsername());
        logger.info("savedUser : {}", savedUser);
        OtpLogDto otpLog = otpLogService.getOtpLogByUsernameAndOtpType(requestOtp.getUsername(), requestOtp.getOtpType());
        logger.info("otpLog : {}", otpLog);
        otpHelper.sendLoginOtp(savedUser.getPhone(), otp);
        otpLog.setOtpValue(otp);
        otpLog.setUsername(requestOtp.getUsername());
        otpLog.setPhone(savedUser.getPhone());
        otpLog.setOtpType(requestOtp.getOtpType());
        otpLog.setOtpStatus(true);
        otpLog.setOtpExpiry(LocalDateTime.now());
        otpLogService.saveOtp(otpLog);
        return ResponseEntity.ok(ResponseUtil.success(null, "otp sent successfully"));
    }

    @PostMapping("/verify-forgot-otp")
    public ResponseEntity<ApiResponse<?>> verifyForgotOtp(
            HttpServletRequest httpRequest,
            @RequestBody JwtAuthRequest authRequest
    ) {
        requestLogService.logRequest(httpRequest);
        logger.info("inside verify-forgot-otp controller");
        boolean otpExists = otpLogService.verifyForgotOtp(authRequest.getUsername(), authRequest.getOtp());
        if (otpExists) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(authRequest.getUsername());
            String token = this.jwtHelper.generateToken(userDetails, AppConstants.FORGOT_TOKEN);

            JwtAuthResponse jwtAuthResponse = new JwtAuthResponse();
            jwtAuthResponse.setToken(token);
            jwtAuthResponse.setMessage(AppConstants.JWT_CREATED);

            return ResponseEntity.ok(ResponseUtil.success(jwtAuthResponse, "Forgot-Otp verified successfully"));
        }
        return ResponseEntity.ok(ResponseUtil.error("Invalid otp"));
    }

    //@PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/change-password/reset")
    public ResponseEntity<ApiResponse<?>> resetPassword(
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
    public ResponseEntity<ApiResponse<?>> forgotPassword(
            HttpServletRequest httpRequest,
            @RequestBody UserDto userDto
    ) {
        requestLogService.logRequest(httpRequest);
        UserDto updatedUser = userService.changePassword(userDto.getUsername(), userDto.getPassword());
        return ResponseEntity.ok(ResponseUtil.success(null, "Password has been reset"));
    }

}
