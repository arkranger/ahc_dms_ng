package ahc.dms.dao.dms.services;

import ahc.dms.dao.dms.entities.Role;
import ahc.dms.dao.dms.entities.User;
import ahc.dms.dao.dms.entities.UserRole;
import ahc.dms.dao.dms.repositories.RoleRepository;
import ahc.dms.dao.dms.repositories.UserRoleRepository;
import ahc.dms.exceptions.ApiException;
import ahc.dms.exceptions.DuplicateResourceException;
import ahc.dms.exceptions.ResourceNotFoundException;
import ahc.dms.payload.response.PageResponse;
import ahc.dms.payload.dto.RoleDto;
import ahc.dms.payload.dto.UserDto;
import ahc.dms.dao.dms.repositories.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserRoleRepository userRoleRepository;

    @Transactional(transactionManager = "dmsTransactionManager")
    public UserDto createUser(UserDto userDto) {
        // username, email, phone
        dataIntegrityValidation(userDto);

        // Extract and validate a single RoleDto from userDto
        Set<RoleDto> roles = userDto.getRoles();
        if (roles == null || roles.isEmpty()) {
            throw new ApiException("role_id must be provided");
        }
        if (roles.size() > 1) {
            throw new ApiException("Only one role_id is allowed");
        }
        // Fetch the Role entity by ID
        Role providedRole = fetchRoleOrThrow(roles.iterator().next().getRoleId());

        // now save new user and the correct user-role mapping
        userDto.setPassword(passwordEncoder.encode(userDto.getPassword()));
        userDto.setStatus(true);
        User newUser = userRepository.saveAndFlush(modelMapper.map(userDto, User.class));
        UserRole userRole = userRoleRepository.saveAndFlush(new UserRole(newUser, providedRole, true));
        RoleDto roleDto = modelMapper.map(userRole.getRole(), RoleDto.class);
        // role should be active at user-role and roles level
        roleDto.setStatus(userRole.getStatus() && providedRole.getStatus());

        //preparing response
        UserDto savedUserDto = modelMapper.map(newUser, UserDto.class);
        savedUserDto.setRoles(new HashSet<>(Collections.singletonList(roleDto)));
        savedUserDto.setUserRoles(null);

        return savedUserDto;
    }

    @Transactional(transactionManager = "dmsTransactionManager")
    public UserDto updateUser(UserDto userDto, Long userId) {

        // Extract and validate a single RoleDto from userDto
        Set<RoleDto> roles = userDto.getRoles();
        if (roles == null || roles.isEmpty()) {
            throw new ApiException("role_id must be provided");
        }
        if (roles.size() > 1) {
            throw new ApiException("Only one role_id is allowed");
        }
        // Fetch the Role entity by ID
        Role providedRole = fetchRoleOrThrow(roles.iterator().next().getRoleId());
        // find and update user
        User updatedUser = userRepository.findById(userId)
                .map(user -> {
                    if (Boolean.FALSE.equals(user.getStatus())) {
                        throw new ApiException("User is deactivated. First activate the user.");
                    }
                    user.setUsername(userDto.getUsername());
                    user.setName(userDto.getName());
                    user.setEmail(userDto.getEmail());
                    user.setAbout(userDto.getAbout());
                    user.setPassword(passwordEncoder.encode(userDto.getPassword()));
                    // Deactivate all roles
                    user.getUserRoles().forEach(userRole -> userRole.setStatus(false));
                    return userRepository.saveAndFlush(user);
                })
                .orElseThrow(() -> new ResourceNotFoundException("User", "Id", userId));

        // if user-role exists then update status to true or create new active user-role
        UserRole userRole = userRoleRepository.findByUserAndRole(updatedUser, providedRole)
                .map(existingUserRole -> {
                    // If exists, update status to true (since previous ones are disabled)
                    existingUserRole.setStatus(true);
                    return userRoleRepository.save(existingUserRole);
                })
                .orElseGet(() ->
                        userRoleRepository.save(new UserRole(updatedUser, providedRole, true)));

        //preparing response
        UserDto updatedUserDto = modelMapper.map(updatedUser, UserDto.class);
        RoleDto roleDto = modelMapper.map(userRole.getRole(), RoleDto.class);
        // role should be active at user-role and roles level
        roleDto.setStatus(userRole.getStatus() && userRole.getRole().getStatus());
        updatedUserDto.setRoles(new HashSet<>(Set.of(roleDto)));
        updatedUserDto.setUserRoles(null);
        return updatedUserDto;
    }

    public UserDto getUserById(Long userId) {
        User user = userRepository
                .findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "Id", userId));

        UserDto userDto = modelMapper.map(user, UserDto.class);
        userDto.setUserRoles(null);
        user.getActiveUserRole()
                .ifPresentOrElse(role -> userDto.setRoles(Set.of(modelMapper.map(role, RoleDto.class))),
                        () -> userDto.setRoles(new HashSet<>()));

        return userDto;
    }

    public UserDto getUserByUsername(String username) {
        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "Username", username));

        UserDto userDto = modelMapper.map(user, UserDto.class);
        userDto.setUserRoles(null);
        user.getActiveUserRole()
                .ifPresentOrElse(role -> userDto.setRoles(Set.of(modelMapper.map(role, RoleDto.class))),
                        () -> userDto.setRoles(new HashSet<>()));

        return userDto;
    }

    public PageResponse<UserDto> getAllUsers(int pageNumber, int pageSize, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
        Page<User> userPage = userRepository.findAll(pageable);

        List<UserDto> userDtos = userPage.getContent().stream()
                .map(user -> modelMapper.map(user, UserDto.class))
                .collect(Collectors.toList());

        PageResponse<UserDto> response = new PageResponse<>();
        response.setContent(userDtos);
        response.setPageNumber(userPage.getNumber());
        response.setPageSize(userPage.getSize());
        response.setTotalElements(userPage.getTotalElements());
        response.setTotalPages(userPage.getTotalPages());
        response.setLastPage(userPage.isLast());

        return response;
    }


    @Transactional(transactionManager = "dmsTransactionManager")
    public void deactivateUser(String username) {
        userRepository.findByUsername(username)
                .map(user -> {
                    if (Boolean.FALSE.equals(user.getStatus())) {
                        throw new ApiException("User is already deactivated");
                    }
                    user.setStatus(false);
                    return userRepository.save(user);
                })
                .orElseThrow(() -> new ResourceNotFoundException("User", "Username Id", username));
    }

    @Transactional(transactionManager = "dmsTransactionManager")
    public void activateUser(String username) {
        userRepository.findByUsername(username)
                .map(user -> {
                    if (Boolean.TRUE.equals(user.getStatus())) {
                        throw new ApiException("User is already active");
                    }
                    user.setStatus(true);
                    return userRepository.save(user);
                })
                .orElseThrow(() -> new ResourceNotFoundException("User", "Username Id", username));
    }

    public UserDto changePassword(String username, String password) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("User", " Username", username));
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
        return modelMapper.map(user, UserDto.class);
    }

    //validation constraints
    private void dataIntegrityValidation(UserDto userDto) {
        // Check if email already exists
        if (userRepository.existsByEmail(userDto.getEmail())) {
            throw new DuplicateResourceException("Email", userDto.getEmail());
        }
        // Check if username already exists
        if (userRepository.existsByUsername(userDto.getUsername())) {
            throw new DuplicateResourceException("Username", userDto.getUsername());
        }
        // Check if phone already exists
        if (userRepository.existsByPhone(userDto.getPhone())) {
            throw new DuplicateResourceException("Phone number", userDto.getPhone());
        }
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
