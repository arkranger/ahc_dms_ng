package ahc.casediary.dao.services;

import ahc.casediary.dao.entities.ObjectMaster;
import ahc.casediary.dao.entities.ObjectRole;
import ahc.casediary.dao.entities.Role;
import ahc.casediary.dao.repositories.ObjectMasterRepository;
import ahc.casediary.dao.repositories.ObjectRoleRepository;
import ahc.casediary.dao.repositories.RoleRepository;
import ahc.casediary.exceptions.ApiException;
import ahc.casediary.exceptions.InvalidRequestException;
import ahc.casediary.exceptions.ResourceNotFoundException;
import ahc.casediary.payload.dto.ObjectMasterDto;
import ahc.casediary.payload.dto.RoleDto;
import ahc.casediary.payload.request.ObjectRoleRequest;
import ahc.casediary.payload.response.ObjectRoleResponse;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

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

    @Transactional
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
