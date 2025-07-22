package ahc.dms.dao.dms.services;

import ahc.dms.dao.dms.entities.Role;
import ahc.dms.dao.dms.repositories.RoleRepository;
import ahc.dms.exceptions.ApiException;
import ahc.dms.exceptions.ResourceNotFoundException;
import ahc.dms.payload.response.PageResponse;
import ahc.dms.payload.dto.RoleDto;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RoleService {

    @Autowired
    private final RoleRepository roleRepository;
    private final Logger logger = LoggerFactory.getLogger(RoleService.class);

    public RoleService(RoleRepository roleRepository){
        this.roleRepository = roleRepository;
    }

    @Autowired
    private ModelMapper modelMapper;

    @Transactional(transactionManager = "dmsTransactionManager")
    public RoleDto createRole(RoleDto roleDto) {
        roleDto.setStatus(true);
        Role savedRole = roleRepository.save(modelMapper.map(roleDto, Role.class));
        return modelMapper.map(savedRole, RoleDto.class);
    }

    @Transactional(transactionManager = "dmsTransactionManager")
    public RoleDto updateRole(RoleDto roleDto, Integer roleId) {
        Role role = roleRepository.findById(roleId).orElseThrow(() -> new ResourceNotFoundException("Role", "Id", roleId));

        role.setRoleName(roleDto.getRoleName());
        role.setStatus(roleDto.getStatus());
        Role updatedRole = roleRepository.save(role);
        return modelMapper.map(updatedRole, RoleDto.class);
    }

//    public List<RoleDto> getAllRoles(Integer pageNumber, Integer pageSize, String sortBy, String sortDir) {
//
//        Sort sort = (sortDir.equalsIgnoreCase("desc")) ? (Sort.by(sortBy).descending()) : (Sort.by(sortBy).ascending());
//        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
//        Page<Role> pageRole = roleRepository.findAll(pageable);
//        List<Role> roles = pageRole.getContent();
//        return roles.stream().map(role -> modelMapper.map(role, RoleDto.class)).collect(Collectors.toList());
//    }

    public PageResponse<RoleDto> getAllRoles(Integer pageNumber, Integer pageSize, String sortBy, String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
        Page<Role> pageRole = roleRepository.findAll(pageable);

        List<RoleDto> roleDtos = pageRole.getContent()
                .stream()
                .map(role -> modelMapper.map(role, RoleDto.class))
                .collect(Collectors.toList());

        PageResponse<RoleDto> pageResponse = new PageResponse<>();
        pageResponse.setContent(roleDtos);
        pageResponse.setPageNumber(pageRole.getNumber());
        pageResponse.setPageSize(pageRole.getSize());
        pageResponse.setTotalElements(pageRole.getTotalElements());
        pageResponse.setTotalPages(pageRole.getTotalPages());
        pageResponse.setLastPage(pageRole.isLast());

        return pageResponse;
    }



    public RoleDto getRoleByRoleId(Integer roleId) {
        Role role = roleRepository.findByRoleId(roleId).orElseThrow(() -> new ResourceNotFoundException("Role", "Role Id", roleId));
        return modelMapper.map(role, RoleDto.class);
    }


    public RoleDto getRoleByRoleName(String roleName) {
        Role role = roleRepository.findByRoleName(roleName).orElseThrow(() -> new ResourceNotFoundException("Role", "Role Name", roleName));
        return modelMapper.map(role, RoleDto.class);
    }

    public void disableRole(Integer roleId) {
        roleRepository.findById(roleId)
                .map(role -> {
                    if (Boolean.FALSE.equals(role.getStatus())) {
                        throw new ApiException("Role is already disabled");
                    } else if (role.getRoleName().equalsIgnoreCase("ROLE_ADMIN")) {
                        throw new ApiException("Admin role can not be disabled");
                    }
                    role.setStatus(false);
                    return roleRepository.save(role);
                })
                .orElseThrow(() -> new ResourceNotFoundException("Role", " Id ", roleId));
    }

    public void enableRole(Integer roleId) {
        roleRepository.findById(roleId)
                .map(role -> {
                    if (Boolean.TRUE.equals(role.getStatus())) {
                        throw new ApiException("Role is already enabled");
                    }
                    role.setStatus(true);
                    return roleRepository.save(role);
                })
                .orElseThrow(() -> new ResourceNotFoundException("Role", " Id ", roleId));
    }

    //for authentication response, send only active role
    public Set<RoleDto> getActiveRoles(UserDetails userDetails) {
        logger.info(userDetails.getUsername());
        Set<Role> activeRoles = userDetails.getAuthorities()
                .stream()
                .map(grantedAuthority ->
                        roleRepository.findByRoleName(grantedAuthority.getAuthority()).get())
                .collect(Collectors.toSet());

        return activeRoles
                .stream()
                .map(activeRole -> modelMapper.map(activeRole, RoleDto.class))
                .collect(Collectors.toSet());
    }
}
