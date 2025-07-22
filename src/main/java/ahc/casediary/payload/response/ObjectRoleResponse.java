package ahc.casediary.payload.response;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import ahc.casediary.payload.dto.ObjectMasterDto;
import ahc.casediary.payload.dto.RoleDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Set;

@NoArgsConstructor
@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ObjectRoleResponse {

    @JsonProperty("object")
    private ObjectMasterDto objectMasterDto;
    @JsonProperty("roles")
    private Set<RoleDto> roleDtos;

}
