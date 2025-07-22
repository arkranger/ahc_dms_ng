package ahc.dms.utils;

import ahc.dms.payload.response.ApiResponse;

import java.util.Map;

public class ResponseUtil {

    public static <T> ApiResponse<T> success(T data, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setStatus(true);
        response.setMessage(message);
        response.setData(data);
        response.setTimestamp(System.currentTimeMillis());
        return response;
    }

    public static <T> ApiResponse<T> error(String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setStatus(false);
        response.setMessage(message);
        response.setData(null);
        response.setTimestamp(System.currentTimeMillis());
        return response;
    }

    public static <T> ApiResponse<T> error(Map<String, String> messages) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setStatus(false);
        response.setMessages(messages);
        response.setData(null);
        response.setTimestamp(System.currentTimeMillis());
        return response;
    }

}
