package ahc.dms.utils;

import ahc.dms.config.AppConstants;
import ahc.dms.exceptions.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Component
public class OtpHelper {

    private final RestTemplate restTemplate = new RestTemplate();
    //private final ObjectMapper objectMapper = new ObjectMapper();
    private final Logger logger = LoggerFactory.getLogger(OtpHelper.class);

    //username=&pass=&sender=HCALLD&sendto=8601837554&templateID=&message=OTP
    public void sendLoginOtp(String phone, String otp) {
        logger.info("phone : {}", phone);
        logger.info("otp : {}", otp);
        //URI
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(AppConstants.LOGIN_OTP_URI)
                .queryParam("username", AppConstants.LOGIN_OTP_USERNAME)
                .queryParam("pass", AppConstants.LOGIN_OTP_PASSWORD)
                .queryParam("sender", AppConstants.LOGIN_OTP_SENDER)
                .queryParam("templateID", AppConstants.LOGIN_OTP_TEMPLATE_ID)
                .queryParam("sendto", phone)
                .queryParam("message", AppConstants.LOGIN_OTP_MSG_BEGIN + otp + AppConstants.LOGIN_OTP_MSG_END);

        URI uri = builder.build().encode().toUri();
        logger.info("uri = {}", uri.getRawQuery());

        String response = restTemplate.getForObject(uri, String.class);
        logger.info("response = {}", response);

        if (!response.contains("success")) {
            throw new ApiException(response);
        }

        /*
        OtpDto responseDto;
        try {
            responseDto = objectMapper.readValue(response, OtpDto.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        //ResponseEntity<OtpDto> response = restTemplate.getForEntity(uri, OtpDto.class);
        //OtpDto responseDto = response.getBody();
        return  responseDto;
        */

    }

}
