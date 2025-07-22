package ahc.casediary.controller;

import ahc.casediary.config.AppConstants;
import ahc.casediary.dao.services.ObjectMasterService;
import ahc.casediary.dao.services.ObjectRoleService;
import ahc.casediary.dao.services.RequestLogService;
import ahc.casediary.payload.dto.ObjectMasterDto;
import ahc.casediary.payload.request.ObjectRoleRequest;
import ahc.casediary.payload.response.GenericResponse;
import ahc.casediary.payload.response.ObjectRoleResponse;
import ahc.casediary.payload.response.PageResponse;
import ahc.casediary.utils.ResponseUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/object")
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
    public ResponseEntity<GenericResponse<ObjectMasterDto>> createObjectMaster(
            HttpServletRequest request,
            @Valid @RequestBody ObjectMasterDto omDto
    ){
        requestLogService.logRequest(request);
        ObjectMasterDto createdDto = omService.createObjectMaster(omDto);
        return ResponseEntity.ok(ResponseUtil.success(createdDto, "object (url) created"));
    }

    @GetMapping("/enable/{omId}")
    public ResponseEntity<GenericResponse<ObjectMasterDto>> enableObjectMaster(
            HttpServletRequest request,
            @PathVariable("omId") Long omId
    ){
        requestLogService.logRequest(request);
        ObjectMasterDto enabledDto = omService.enableObjectMaster(omId);
        return ResponseEntity.ok(ResponseUtil.success(enabledDto, "object (url) enabled"));
    }

    @GetMapping("/disable/{omId}")
    public ResponseEntity<GenericResponse<ObjectMasterDto>> disableObjectMaster(
            HttpServletRequest request,
            @PathVariable("omId") Long omId
    ){
        requestLogService.logRequest(request);
        ObjectMasterDto disabledDto = omService.disableObjectMaster(omId);
        return ResponseEntity.ok(ResponseUtil.success(disabledDto, "object (url) disabled"));
    }

    // Actions With Roles

    @PostMapping("/register")
    public ResponseEntity<GenericResponse<ObjectRoleResponse>> createObjectRole(
            HttpServletRequest request,
            @Valid @RequestBody ObjectRoleRequest orRequest
    ){
        requestLogService.logRequest(request);
        ObjectRoleResponse objectRoleResponse = orService.createObjectRole(orRequest);
        return ResponseEntity.ok(ResponseUtil.success(objectRoleResponse, "object-role mapping created"));
    }

    @PostMapping("/assign-role")
    public ResponseEntity<GenericResponse<ObjectRoleResponse>> assignRoleToObject(
            HttpServletRequest request,
            @Valid @RequestBody ObjectRoleRequest orRequest
    ){
        requestLogService.logRequest(request);
        ObjectRoleResponse objectRoleResponse = orService.assignRoleToObject(orRequest);
        return ResponseEntity.ok(ResponseUtil.success(objectRoleResponse, "role assigned"));
    }

    @PostMapping("/de-assign-role")
    public ResponseEntity<GenericResponse<ObjectRoleResponse>> deAssignRoleFromObject(
            HttpServletRequest request,
            @Valid @RequestBody ObjectRoleRequest orRequest
    ){
        requestLogService.logRequest(request);
        ObjectRoleResponse objectRoleResponse = orService.deAssignRoleFromObject(orRequest);
        return ResponseEntity.ok(ResponseUtil.success(objectRoleResponse, "role de-assigned"));
    }

    // GET ALL OBJECT URI
    @GetMapping("/")
    public ResponseEntity<GenericResponse<PageResponse<ObjectMasterDto>>> getAllUriObject(
            HttpServletRequest httpServletRequest,
            @RequestParam(value = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
            @RequestParam(value = "pageSize", defaultValue =  AppConstants.PAGE_SIZE, required = false) Integer pageSize,
            @RequestParam(value = "sortBy", defaultValue = AppConstants.SORT_OBJECT_BY, required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = AppConstants.SORT_DIR, required = false) String sortDir)
    {
        requestLogService.logRequest(httpServletRequest);
        PageResponse<ObjectMasterDto> objectMasterDto=omService.getAllUriObjects(pageNumber,pageSize,sortBy,sortDir);
        return ResponseEntity.ok(ResponseUtil.success(objectMasterDto,"***** Object uri fetched successfully ****"));
    }

}
