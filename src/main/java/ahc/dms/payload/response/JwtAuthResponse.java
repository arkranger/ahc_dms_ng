package ahc.dms.payload.response;

import ahc.dms.payload.dto.UserDto;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JwtAuthResponse {

    private String token;
    private String message;
    private UserDto user;
}
