package ahc.dms.dao.dms.services;

import ahc.dms.dao.dms.entities.ObjectMaster;
import ahc.dms.dao.dms.entities.ObjectRole;
import ahc.dms.dao.dms.entities.Role;
import ahc.dms.dao.dms.repositories.*;
import ahc.dms.exceptions.ApiException;
import ahc.dms.exceptions.InvalidRequestException;
import ahc.dms.exceptions.ResourceNotFoundException;
import ahc.dms.payload.dto.ObjectMasterDto;
import ahc.dms.payload.dto.RoleDto;
import ahc.dms.payload.request.ObjectRoleRequest;
import ahc.dms.payload.response.ObjectRoleResponse;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class ObjectRoleService {

    @Autowired
    private ObjectMasterRepository omRepository;
    @Autowired
    private ObjectRoleRepository orRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private ModelMapper modelMapper;
    private final Logger logger = LoggerFactory.getLogger(ObjectRoleService.class);

    @Transactional(transactionManager = "dmsTransactionManager")
    public ObjectRoleResponse createObjectRole(@Valid ObjectRoleRequest orRequest) {

        ObjectMasterDto omDto =  orRequest.getObjectMasterDto();
        Set<Role> roles = new HashSet<>();
        Set<RoleDto> roleDtos = new HashSet<>();

        // Get all the distinct roles in the Set<Role>
        for (RoleDto roleDto : orRequest.getRoleDtos()) {
            if (roleDto.getRoleId() != null) {
                Role role = fetchRoleOrThrow(roleDto.getRoleId());
                roles.add(role);
            }
        }
        logger.info("No of distinct roles : {}", roles.size());

        ObjectMaster om = omRepository
                .findByRequestUriAndRequestMethod(omDto.getRequestUri(), omDto.getRequestMethod())
                .map(existingOm -> {
                    if (Boolean.FALSE.equals(existingOm.getStatus()))
                        throw new ApiException("Url is disabled");
                    return existingOm;
                })
                .orElseGet(() -> {
                    ObjectMaster newOm =  new ObjectMaster();
                    newOm.setRequestMethod(omDto.getRequestMethod());
                    newOm.setRequestUri(omDto.getRequestUri());
                    newOm.setStatus(true);
                    return omRepository.saveAndFlush(newOm);
                });

        for (Role role : roles) {
            orRepository.findByObjectMasterAndRole(om, role)
                            .orElseGet(() -> {
                                // To avoid duplicate roles in response
                                roleDtos.add(modelMapper.map(role, RoleDto.class));
                                return orRepository.save(new ObjectRole(om, role, true));
                            });
        }

        if (roleDtos.isEmpty()) {
            throw new ApiException("Provided object-role mappings already exists");
        }

        ObjectRoleResponse orResponse = new ObjectRoleResponse();
        orResponse.setObjectMasterDto(modelMapper.map(om, ObjectMasterDto.class));
        orResponse.setRoleDtos(roleDtos);

        return orResponse;

    }

    public ObjectRoleResponse assignRoleToObject(@Valid ObjectRoleRequest orRequest) {

        ObjectMasterDto omDto = orRequest.getObjectMasterDto();
        Set<Role> roles = new HashSet<>();
        Set<RoleDto> roleDtos = new HashSet<>();

        ObjectMaster existingOm = fetchObjectMasterOrThrow(omDto.getRequestUri(), omDto.getRequestMethod());

        // Get all the distinct roles in the Set<Role>
        for (RoleDto roleDto : orRequest.getRoleDtos()) {
            if (roleDto.getRoleId() != null) {
                Role role = fetchRoleOrThrow(roleDto.getRoleId());
                roles.add(role);
            }
        }
        logger.info("No of distinct roles : {}", roles.size());

        for (Role role : roles) {
            orRepository.findByObjectMasterAndRole(existingOm, role)
                    .orElseGet(() -> {
                                roleDtos.add(modelMapper.map(role, RoleDto.class));
                                return orRepository.saveAndFlush(new ObjectRole(existingOm, role, true));
                    });
        }

        ObjectRoleResponse orResponse =  new ObjectRoleResponse();
        orResponse.setObjectMasterDto(modelMapper.map(existingOm, ObjectMasterDto.class));
        orResponse.setRoleDtos(roleDtos);
        return orResponse;
    }

    public ObjectRoleResponse deAssignRoleFromObject(@Valid ObjectRoleRequest orRequest) {
        ObjectMasterDto omDto = orRequest.getObjectMasterDto();
        Set<Role> roles = new HashSet<>();
        Set<RoleDto> roleDtos = new HashSet<>();

        ObjectMaster existingOm = fetchObjectMasterOrThrow(omDto.getRequestUri(), omDto.getRequestMethod());
        // Get all the distinct roles in the Set<Role>
        for (RoleDto roleDto : orRequest.getRoleDtos()) {
            if (roleDto.getRoleId() != null) {
                Role role = fetchRoleOrThrow(roleDto.getRoleId());
                roles.add(role);
            }
        }
        logger.info("No of distinct roles : {}", roles.size());

        // First check if all the given object-role mapping exist
        for (Role role : roles) {
            orRepository.findByObjectMasterAndRole(existingOm, role)
                    .map(objectRole -> {
                        if (Boolean.FALSE.equals(objectRole.getStatus())) {
                            throw new InvalidRequestException(
                                    existingOm.getRequestUri(),
                                    existingOm.getRequestMethod(),
                                    " RoleId " + role.getRoleId() + " already de-assigned");
                        }
                        return objectRole;
                    })
                    .orElseThrow(() -> new ResourceNotFoundException(
                            existingOm.getRequestUri() + ":" + existingOm.getRequestMethod(),
                            "roleId",
                            role.getRoleId()
                    ));
        }

        for (Role role : roles) {
            orRepository.findByObjectMasterAndRole(existingOm, role)
                    .map(objectRole -> {
                        RoleDto roleDto = modelMapper.map(role, RoleDto.class);
                        roleDto.setStatus(false);
                        roleDtos.add(roleDto);
                        objectRole.setStatus(false);
                        return orRepository.save(objectRole);
                    });
        }

        ObjectRoleResponse orResponse =  new ObjectRoleResponse();
        orResponse.setObjectMasterDto(modelMapper.map(existingOm, ObjectMasterDto.class));
        orResponse.setRoleDtos(roleDtos);
        return orResponse;


    }

    private ObjectMaster fetchObjectMasterOrThrow(String requestUri, String requestMethod) {
        return omRepository.findByRequestUriAndRequestMethod(requestUri, requestMethod)
                .map(objectMaster -> {
                    if (Boolean.FALSE.equals(objectMaster.getStatus())) {
                        throw new ApiException("Object-url is disabled");
                    }
                    return objectMaster;
                })
                .orElseThrow(() -> new ResourceNotFoundException(requestUri, "method", requestMethod));
    }

    // UTILITY FUNCTIONS
    private Role fetchRoleOrThrow(Integer roleId) {
        return Optional.ofNullable(roleId)
                .map(id -> roleRepository.findByRoleId(id)
                        .map(role -> {
                            if (Boolean.FALSE.equals(role.getStatus())) {
                                throw new ApiException("Role is disabled");
                            }
                            return role;
                        })
                        .orElseThrow(() -> new ResourceNotFoundException("Role", "Role Id", id)))
                .orElseThrow(() -> new ApiException("Role Id cannot be null"));
    }

}
