package ahc.dms.dao.edms.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "token_logs")
@Getter
@Setter
@NoArgsConstructor
public class TokenLogs {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "token_seq")
    @SequenceGenerator(
            name = "token_seq",
            sequenceName = "token_sequence", // DB sequence name
            allocationSize = 1
    )
    @Column(name = "token_id")
    private Long tokenId;
    @Column(name = "username", nullable = false)
    private String username;
    private String jwtToken;
    private Date expirationDate;
    private boolean tokenStatus;

}
