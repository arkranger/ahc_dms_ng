package ahc.dms.dao.dms.services;

import ahc.dms.dao.dms.entities.Role;
import ahc.dms.dao.dms.entities.User;
import ahc.dms.dao.dms.entities.UserRole;
import ahc.dms.dao.dms.repositories.RoleRepository;
import ahc.dms.dao.dms.repositories.UserRepository;
import ahc.dms.dao.dms.repositories.UserRoleRepository;
import ahc.dms.exceptions.ApiException;
import ahc.dms.exceptions.ResourceNotFoundException;
import ahc.dms.payload.dto.RoleDto;
import ahc.dms.payload.dto.UserDto;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;

@Service
public class UserRoleService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private UserRoleRepository userRoleRepository;

    @Transactional(transactionManager = "dmsTransactionManager")
    public UserDto assignRole(String username, Integer roleId) {
        //check for existing user and role
        User existingUser = fetchUserOrThrow(username);
        Role existingRole = fetchRoleOrThrow(roleId);

        //check for existing role-mapping
        Optional<UserRole> existingUserRole = userRoleRepository.findByUserAndRole(existingUser, existingRole);
        if (existingUserRole.isPresent() && existingUserRole.get().getStatus()) {
            // if an active user-role mapping already exists
            throw new ApiException("Mapping already exists");
        } else if (existingUserRole.isPresent()) {
            // if stale user-role mapping is present
            // first deactivate all other user-role mapping
            existingUser.getUserRoles().forEach(userRole -> userRole.setStatus(false));
            userRepository.saveAndFlush(existingUser);
            // activate the provided user-role mapping
            existingUserRole.get().setStatus(true);
            userRoleRepository.saveAndFlush(existingUserRole.get());
        } else {
            //if no user-role mapping is present, then create a new one
            //first deactivate all the previous user-role mapping
            existingUser.getUserRoles().forEach(userRole -> userRole.setStatus(false));
            userRepository.saveAndFlush(existingUser);
            //create a new active user-role mapping
            UserRole newUserRole = new UserRole(existingUser, existingRole, true);
            userRoleRepository.saveAndFlush(newUserRole);
        }

        // Prepare response
        UserDto theUserDto = modelMapper.map(existingUser, UserDto.class);
        RoleDto theRoleDto = modelMapper.map(existingRole, RoleDto.class);
        theUserDto.setRoles(new HashSet<>(Collections.singletonList(theRoleDto)));
        theUserDto.setUserRoles(null);
        return theUserDto;
    }

    @Transactional(transactionManager = "dmsTransactionManager")
    public UserDto deassignRole(String username, Integer roleId) {

        //check for existing user and role
        User existingUser = fetchUserOrThrow(username);
        Role existingRole = fetchRoleOrThrow(roleId);

        //check for existing role-mapping
        // if found active then disable it and if not found throw exception
        UserRole updatedUserRole = userRoleRepository.findByUserAndRole(existingUser, existingRole)
                .map(userRole -> {
                    if (!userRole.getStatus()) {
                        throw new ApiException("Role already de-assigned");
                    }
                    userRole.setStatus(false);
                    return userRoleRepository.saveAndFlush(userRole);
                    //refresh context
                    //return userRoleRepository.findById(userRole.getUrId()).orElseThrow();
                }).orElseThrow(() -> new ApiException("User has never been assigned given Role"));

        // Prepare response
        UserDto theUserDto = modelMapper.map(existingUser, UserDto.class);
        RoleDto theRoleDto = modelMapper.map(existingRole, RoleDto.class);
        //set the status from the user-role mapping
        theRoleDto.setStatus(updatedUserRole.getStatus());
        theUserDto.setRoles(Collections.singleton(theRoleDto));
        theUserDto.setUserRoles(null);
        return theUserDto;
    }

    private User fetchUserOrThrow(String username) {
        return Optional.ofNullable(username)
                .map(lId -> userRepository.findByUsername(lId)
                        .map(user -> {
                            if (Boolean.FALSE.equals(user.getStatus())) {
                                throw new ApiException("User is disabled");
                            }
                            return user;
                        })
                        .orElseThrow(() -> new ResourceNotFoundException("User", "Login Id", username)))
                .orElseThrow(() -> new ApiException("Login Id cannot be null"));
    }

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
