package ahc.dms.controller;

import ahc.dms.config.AppConstants;
import ahc.dms.dao.dms.services.RequestLogService;
import ahc.dms.dao.dms.services.UserRoleService;
import ahc.dms.payload.response.ApiResponse;
import ahc.dms.payload.response.PageResponse;
import ahc.dms.payload.dto.UserDto;
import ahc.dms.dao.dms.services.UserService;
import ahc.dms.utils.ResponseUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dms/users")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private UserRoleService userRoleService;
    @Autowired
    private RequestLogService requestLogService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<UserDto>> createUser(
            HttpServletRequest httpRequest,
            @Valid @RequestBody UserDto userDto
    ){
        requestLogService.logRequest(httpRequest);
        UserDto createdUserDto = userService.createUser(userDto);
        return ResponseEntity.ok(ResponseUtil.success(createdUserDto, "user created"));

    }

    @PutMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserDto>> updateUser(
            HttpServletRequest httpRequest,
            @Valid @RequestBody UserDto userDto,
            @PathVariable("userId") Long userId)
    {
        requestLogService.logRequest(httpRequest);
        UserDto updatedUser = userService.updateUser(userDto, userId);
        return ResponseEntity.ok(ResponseUtil.success(updatedUser, "user updated"));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/deactivate/{username}")
    public ResponseEntity<ApiResponse<?>> deactivateUser(
            @PathVariable("username") String username,
            HttpServletRequest httpRequest
    ){
        requestLogService.logRequest(httpRequest);
        userService.deactivateUser(username);
        return ResponseEntity.ok(ResponseUtil.success(null, "user deactivated"));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/activate/{username}")
    public ResponseEntity<ApiResponse<?>> activateUser(
            @PathVariable("username") String username,
            HttpServletRequest httpRequest
    ){
        requestLogService.logRequest(httpRequest);
        userService.activateUser(username);
        return ResponseEntity.ok(ResponseUtil.success(null, "user activated"));
    }

@GetMapping("/")
public ResponseEntity<ApiResponse<PageResponse<UserDto>>> getAllUsers(
        HttpServletRequest httpRequest,
        @RequestParam(value = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
        @RequestParam(value = "pageSize", defaultValue =  AppConstants.PAGE_SIZE, required = false) Integer pageSize,
        @RequestParam(value = "sortBy", defaultValue = AppConstants.SORT_USER_BY, required = false) String sortBy,
        @RequestParam(value = "sortDir", defaultValue = AppConstants.SORT_DIR, required = false) String sortDir) {
    requestLogService.logRequest(httpRequest);
    PageResponse<UserDto> users = userService.getAllUsers(pageNumber, pageSize, sortBy, sortDir);
    return ResponseEntity.ok(ResponseUtil.success(users, "user fetched"));
}


    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserDto>> getUserProfile(@PathVariable("userId") Long userId){
        return ResponseEntity.ok(ResponseUtil.success(userService.getUserById(userId), "user fetched"));
    }

}
