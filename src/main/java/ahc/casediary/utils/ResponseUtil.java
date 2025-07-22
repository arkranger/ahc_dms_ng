package ahc.casediary.utils;

import ahc.casediary.payload.response.GenericResponse;

import java.util.Map;

public class ResponseUtil {

    public static <T> GenericResponse<T> success(T data, String message) {
        GenericResponse<T> response = new GenericResponse<>();
        response.setStatus(true);
        response.setMessage(message);
        response.setData(data);
        response.setTimestamp(System.currentTimeMillis());
        return response;
    }

    public static <T> GenericResponse<T> error(String message) {
        GenericResponse<T> response = new GenericResponse<>();
        response.setStatus(false);
        response.setMessage(message);
        response.setData(null);
        response.setTimestamp(System.currentTimeMillis());
        return response;
    }

    public static <T> GenericResponse<T> error(Map<String, String> messages) {
        GenericResponse<T> response = new GenericResponse<>();
        response.setStatus(false);
        response.setMessages(messages);
        response.setData(null);
        response.setTimestamp(System.currentTimeMillis());
        return response;
    }

}
