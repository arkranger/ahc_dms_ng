package ahc.dms.dao.dms.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "role_master")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Role {

    @Id
    @Column(name = "role_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "role_seq")
    @SequenceGenerator(
            name = "role_seq",
            sequenceName = "role_sequence", // DB sequence name
            allocationSize = 1
    )
    private Integer roleId;
    @Column(name = "role_name", unique = true, nullable = false)
    private String roleName;
    @Column(nullable = false)
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

    // Optional reverse mapping
//    @OneToMany(mappedBy = "role")
//    private Set<UserRole> userRoles = new HashSet<>();

    @PrePersist
    @PreUpdate
    public void capitalizeRoleName() {
        if (this.roleName != null) {
            //this.roleName = "ROLE_".concat(roleName.toUpperCase());
            this.roleName = roleName.toUpperCase();
        }
    }

    // Getter, Setter and Constructors


    public Role(Integer roleId, String roleName, Boolean status) {
        this.roleId = roleId;
        this.roleName = roleName;
        this.status = status;
    }

    public Role(String roleName, Boolean status) {
        this.roleName = roleName;
        this.status = status;
    }
}
