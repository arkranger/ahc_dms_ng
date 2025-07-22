package ahc.casediary.dao.services;

import ahc.casediary.dao.entities.RequestLog;
import ahc.casediary.dao.repositories.RequestLogRepository;
import ahc.casediary.utils.RequestUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RequestLogService {

    @Autowired
    private RequestLogRepository requestLogRepository;

    public void logRequest(HttpServletRequest request) {
        RequestLog requestLog = new RequestLog();
        requestLog.setIpAddress(RequestUtil.getClientIp(request));
        requestLog.setEndpoint(request.getRequestURI());
        requestLog.setMethod(request.getMethod());
        requestLogRepository.save(requestLog);
    }
}
