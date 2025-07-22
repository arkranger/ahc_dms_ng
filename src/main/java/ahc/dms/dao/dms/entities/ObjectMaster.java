package ahc.dms.dao.dms.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "object_master")
@Getter
@Setter
@NoArgsConstructor
public class ObjectMaster {

    // COLUMNS
    @Id
    @Column(name = "om_id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "om_seq")
    @SequenceGenerator(
            name = "om_seq",
            sequenceName = "om_sequence", // This is the name of the DB sequence
            allocationSize = 1 // Optional: 1 means no batch caching
    )    private Long omId;

    @Column(name = "request_uri")
    private String requestUri;
    @Column(name = "request_method")
    private String requestMethod;
    @Column(name = "status", nullable = false, columnDefinition = "boolean default true")
    private Boolean status;

    // MAPPING TO OBJECT-ROLES
    @OneToMany(
            mappedBy = "objectMaster",
            orphanRemoval = true,
            fetch = FetchType.EAGER
    )
    private Set<ObjectRole> objectRoles = new HashSet<>();

}