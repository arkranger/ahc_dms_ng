package ahc.dms.dao.dms.services;

import ahc.dms.dao.dms.entities.RequestLog;
import ahc.dms.dao.dms.repositories.RequestLogRepository;
import ahc.dms.utils.RequestUtil;
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
