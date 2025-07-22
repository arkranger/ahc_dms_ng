package ahc.dms.controller;

import ahc.dms.dao.dms.services.*;
import ahc.dms.payload.request.ObjectRoleRequest;
import ahc.dms.payload.response.ApiResponse;
import ahc.dms.payload.dto.ObjectMasterDto;
import ahc.dms.payload.response.ObjectRoleResponse;
import ahc.dms.utils.ResponseUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dms/object")
@PreAuthorize("hasRole('ADMIN')")
public class ObjectController {

    @Autowired
    private ObjectRoleService orService;
    @Autowired
    private ObjectMasterService omService;
    @Autowired
    private RequestLogService requestLogService;

    // Actions without Roles
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<ObjectMasterDto>> createObjectMaster(
            HttpServletRequest request,
            @Valid @RequestBody ObjectMasterDto omDto
    ){
        requestLogService.logRequest(request);
        ObjectMasterDto createdDto = omService.createObjectMaster(omDto);
        return ResponseEntity.ok(ResponseUtil.success(createdDto, "object (url) created"));
    }

    @GetMapping("/enable/{omId}")
    public ResponseEntity<ApiResponse<ObjectMasterDto>> enableObjectMaster(
            HttpServletRequest request,
            @PathVariable("omId") Long omId
    ){
        requestLogService.logRequest(request);
        ObjectMasterDto enabledDto = omService.enableObjectMaster(omId);
        return ResponseEntity.ok(ResponseUtil.success(enabledDto, "object (url) enabled"));
    }

    @GetMapping("/disable/{omId}")
    public ResponseEntity<ApiResponse<ObjectMasterDto>> disableObjectMaster(
            HttpServletRequest request,
            @PathVariable("omId") Long omId
    ){
        requestLogService.logRequest(request);
        ObjectMasterDto disabledDto = omService.disableObjectMaster(omId);
        return ResponseEntity.ok(ResponseUtil.success(disabledDto, "object (url) disabled"));
    }

    // Actions With Roles

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<ObjectRoleResponse>> createObjectRole(
            HttpServletRequest request,
            @Valid @RequestBody ObjectRoleRequest orRequest
    ){
        requestLogService.logRequest(request);
        ObjectRoleResponse objectRoleResponse = orService.createObjectRole(orRequest);
        return ResponseEntity.ok(ResponseUtil.success(objectRoleResponse, "object-role mapping created"));
    }

    @PostMapping("/assign-role")
    public ResponseEntity<ApiResponse<ObjectRoleResponse>> assignRoleToObject(
            HttpServletRequest request,
            @Valid @RequestBody ObjectRoleRequest orRequest
    ){
        requestLogService.logRequest(request);
        ObjectRoleResponse objectRoleResponse = orService.assignRoleToObject(orRequest);
        return ResponseEntity.ok(ResponseUtil.success(objectRoleResponse, "role assigned"));
    }

    @PostMapping("/de-assign-role")
    public ResponseEntity<ApiResponse<ObjectRoleResponse>> deAssignRoleFromObject(
            HttpServletRequest request,
            @Valid @RequestBody ObjectRoleRequest orRequest
    ){
        requestLogService.logRequest(request);
        ObjectRoleResponse objectRoleResponse = orService.deAssignRoleFromObject(orRequest);
        return ResponseEntity.ok(ResponseUtil.success(objectRoleResponse, "role de-assigned"));
    }

}
