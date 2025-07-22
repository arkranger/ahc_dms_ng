package ahc.dms.payload.dto;

import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResetPasswordDto {

    @NotBlank
    @Size(min=4, message = "Must be greater than 4 characters.")
    private String username;

    //@Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&])[A-Za-z\\d@$!%*#?&]{8,}$")
    @NotBlank
    @Size(min=4, message = "Must be greater than 4 characters.")
    @JsonProperty("old_password")
    private String oldPassword;

    @NotBlank
    @Size(min=4, message = "Must be greater than 4 characters.")
    @JsonProperty("new_password")
    private String newPassword;


}
