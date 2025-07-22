package ahc.dms.payload.dto;

import com.fasterxml.jackson.annotation.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OtpLogDto {

    @JsonIgnore
    private Long version;
    private Long otpId;
    private String username;
    @JsonProperty("otp_type")
    private String otpType;
    private String phone;
    @JsonProperty("otp_value")
    private String otpValue;
    @JsonProperty("otp_expiry")
    private LocalDateTime otpExpiry;
    @JsonProperty("otp_status")
    private Boolean otpStatus;
    private String status;
    private String message;
    @JsonProperty("sms_id")
    private String smsId;

    // audit fields
    @JsonProperty("created_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

}
