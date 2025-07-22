package ahc.dms.dao.dms.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Table(name = "user_master")
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class User implements UserDetails {

    @Version  // ← Optimistic lock column
    private Long version = 0L;

    @Id
    @Column(name = "user_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq")
    @SequenceGenerator(
            name = "user_seq",
            sequenceName = "user_sequence", // This is the name of the DB sequence
            allocationSize = 1 // Optional: 1 means no batch caching
    )
    private Long userId;

    @Column(name = "username", nullable = false, length=100, unique = true)
    private String username;
    @Column(name = "name", nullable = false, length=100)
    private String name;
    @Column(name = "email", nullable = false, length=100, unique = true)
    private String email;
    @Column(name = "phone", nullable = false, length=100, unique = true)
    private String phone;
    @Column(name = "password", nullable = false, length=100)
    private String password;
    private String about;
    @Column(name = "status", nullable = false, columnDefinition = "boolean default true")
    private Boolean status = true;

    // Audit Fields
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private String createdBy;
    @LastModifiedBy
    @Column(name = "updated_by")
    private String updatedBy;

    @OneToMany(
            mappedBy = "user",
            orphanRemoval = true,
            fetch = FetchType.EAGER
    )
    private Set<UserRole> userRoles = new HashSet<>();

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        // find the active role in user-role as well as in role (master) entities
        return this.getUserRoles().stream()
                .filter(userRole -> userRole.getStatus() && userRole.getRole().getStatus())
                .map(userRole -> new SimpleGrantedAuthority(userRole.getRole().getRoleName()))
                .collect(Collectors.toList());
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }


    /*
    public void assignNewActiveRole (Role role) {
        //deactivate all userroles first
        this.getUserRoles().forEach(userRole -> userRole.setStatus(false));
        //check if user already has given role (just inactive)
        Optional<UserRole> existingUserRole = this.getUserRoles()
                .stream()
                .filter(userRole -> userRole.getRole().equals(role))
                .findFirst();

        if (existingUserRole.isPresent()) {
            //reactivate role mapping
            existingUserRole.get().setStatus(true);
        } else {
            //create new active role
            UserRole newUserRole = new UserRole(this, role, true);
            this.getUserRoles().add(newUserRole);
        }
    }

    */

    public void deactivateAllUserRoles(){
        this.getUserRoles().forEach(userRole -> userRole.setStatus(false));
    }

    public Optional<Role> getActiveUserRole() {
        return this.getUserRoles()
                .stream()
                .filter(userRole -> userRole.getStatus())
                .map(userRole -> userRole.getRole())
                .findFirst();
    }

}
