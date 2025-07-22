package ahc.dms.controller;

import ahc.dms.config.AppConstants;
import ahc.dms.dao.dms.services.RequestLogService;
import ahc.dms.dao.dms.services.RoleService;
import ahc.dms.dao.dms.services.UserRoleService;
import ahc.dms.payload.dto.RoleDto;
import ahc.dms.payload.dto.UserDto;
import ahc.dms.payload.response.ApiResponse;
import ahc.dms.payload.response.PageResponse;
import ahc.dms.utils.ResponseUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dms/role")
public class RoleController {

    @Autowired
    private RoleService roleService;
    @Autowired
    private UserRoleService userRoleService;
    @Autowired
    private RequestLogService requestLogService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/")
    public ResponseEntity<ApiResponse<RoleDto>> createRole(
            HttpServletRequest request,
            @Valid @RequestBody RoleDto roleDto
    ){
        requestLogService.logRequest(request);
        RoleDto createdRoleDto = roleService.createRole(roleDto);
        return ResponseEntity.ok(ResponseUtil.success(createdRoleDto, "role created"));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{roleId}")
    public ResponseEntity<ApiResponse<RoleDto>> updateRole(
            HttpServletRequest httpRequest,
            @Valid @RequestBody RoleDto roleDto,
            @PathVariable("roleId") Integer roleId
    ) {
        requestLogService.logRequest(httpRequest);
        RoleDto updatedRole = roleService.updateRole(roleDto, roleId);
        return ResponseEntity.ok(ResponseUtil.success(updatedRole, "role updated"));
    }

    @GetMapping("/")
    public ResponseEntity<ApiResponse<PageResponse<RoleDto>>> getRoles(
            @RequestParam(value = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(value = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(value = "sortBy", defaultValue = AppConstants.SORT_ROLE_BY, required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = AppConstants.SORT_DIR, required = false) String sortDir
    ) {
        PageResponse<RoleDto> pageResponse = roleService.getAllRoles(pageNumber, pageSize, sortBy, sortDir);
        return ResponseEntity.ok(ResponseUtil.success(pageResponse, "Role list fetched successfully"));
    }


    @GetMapping("/roleId/{roleId}")
    public ResponseEntity<ApiResponse<RoleDto>> getRoleById(@PathVariable("roleId") Integer roleId) {
        RoleDto role = roleService.getRoleByRoleId(roleId);
        return ResponseEntity.ok(ResponseUtil.success(role, "role found"));
    }

    @GetMapping("/roleName/{roleName}")
    public ResponseEntity<ApiResponse<RoleDto>> getRoleByName(@PathVariable("roleName") String roleName) {
        RoleDto role = roleService.getRoleByRoleName(roleName);
        return ResponseEntity.ok(ResponseUtil.success(role, "role found"));
    }

    @GetMapping("/disable/{roleId}")
    public ResponseEntity<ApiResponse<?>> disableRole(
            HttpServletRequest httpRequest,
            @PathVariable("roleId") Integer roleId
    ){
        requestLogService.logRequest(httpRequest);
        roleService.disableRole(roleId);
        return ResponseEntity.ok(ResponseUtil.success(null, "role disabled"));
    }

    @GetMapping("/enable/{roleId}")
    public ResponseEntity<ApiResponse<?>> enableRole(
            HttpServletRequest httpRequest,
            @PathVariable("roleId") Integer roleId
    ){
        requestLogService.logRequest(httpRequest);
        roleService.enableRole(roleId);
        return ResponseEntity.ok(ResponseUtil.success(null, "role enabled"));
    }

    @GetMapping("/assign-role")
    public ResponseEntity<ApiResponse<UserDto>> assignRole(
            HttpServletRequest httpRequest,
            @RequestParam(value = "username") String username,
            @RequestParam(value = "roleId") Integer roleId
    ) {
        requestLogService.logRequest(httpRequest);
        UserDto updatedUserDto = userRoleService.assignRole(username, roleId);
        return ResponseEntity.ok(ResponseUtil.success(updatedUserDto, "role assigned"));
    }

    @GetMapping("/deassign-role")
    public ResponseEntity<ApiResponse<UserDto>> deassignRole(
            HttpServletRequest httpRequest,
            @RequestParam(value = "username") String username,
            @RequestParam(value = "roleId") Integer roleId
    ) {
        requestLogService.logRequest(httpRequest);
        UserDto updatedUserDto = userRoleService.deassignRole(username, roleId);
        return ResponseEntity.ok(ResponseUtil.success(updatedUserDto, "role de-assigned"));
    }

}
