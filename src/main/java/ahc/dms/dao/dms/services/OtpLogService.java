package ahc.dms.dao.dms.services;

import ahc.dms.config.AppConstants;
import ahc.dms.dao.dms.entities.OtpLog;
import ahc.dms.dao.dms.repositories.OtpLogRepository;
import ahc.dms.payload.dto.OtpLogDto;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class OtpLogService {

    @Autowired
    private OtpLogRepository otpLogRepository;
    @Autowired
    private ModelMapper modelMapper;

    @Transactional(transactionManager = "dmsTransactionManager")
    public OtpLogDto saveOtp(OtpLogDto otpLogDto) {
        OtpLog newOtp = otpLogRepository.save(modelMapper.map(otpLogDto, OtpLog.class));
        return modelMapper.map(newOtp, OtpLogDto.class);
    }

    public OtpLogDto getOtpLogByUsernameAndOtpType(String username, String otpType) {
        OtpLog otpLog = otpLogRepository.findByUsernameAndOtpType(username, otpType).orElse(new OtpLog());
        return modelMapper.map(otpLog, OtpLogDto.class);
    }

    public boolean verifyLoginOtp(String username, String otp) {
        return otpLogRepository.findByUsernameAndOtpTypeAndOtpValueAndOtpStatusTrue(username, AppConstants.LOGIN_TOKEN, otp).isPresent();
    }

    public boolean verifyForgotOtp(String username, String otp) {
        Optional<OtpLog> otpLog = otpLogRepository.findByUsernameAndOtpTypeAndOtpValueAndOtpStatusTrue(username, AppConstants.FORGOT_TOKEN, otp);
        if (otpLog.isPresent()) {
            OtpLog log = otpLog.get();
            // disable otp so that it can't be used more than once and more importantly for login auth
            log.setOtpStatus(false);
            otpLogRepository.save(log);
            return true;
        } else {
            return false;
        }
    }
}
