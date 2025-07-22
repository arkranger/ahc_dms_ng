package ahc.casediary.exceptions;

import ahc.casediary.payload.response.GenericResponse;
import ahc.casediary.utils.ResponseUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // for database errors
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<GenericResponse<?>> duplicateResourceExceptionHandler(DuplicateResourceException ex) {
        String message = ex.getMessage();
        return new ResponseEntity<>(ResponseUtil.error(message), HttpStatus.BAD_REQUEST);
    }

    // for database errors
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<GenericResponse<?>> resourceNotFoundExceptionHandler(ResourceNotFoundException ex) {
        String message = ex.getMessage();
        return new ResponseEntity<>(ResponseUtil.error(message), HttpStatus.NOT_FOUND);
    }

    // for database errors
    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<GenericResponse<?>> invalidRequestExceptionHandler(InvalidRequestException ex) {
        String message = ex.getMessage();
        return new ResponseEntity<>(ResponseUtil.error(message), HttpStatus.BAD_REQUEST);
    }

    // for request validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GenericResponse<Map<String, String>>> handleMethodArgsNotValidException(MethodArgumentNotValidException ex) {
        Map<String, String>  response = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError)error).getField();
            String message = error.getDefaultMessage();
            response.put(fieldName, message);
        });

        return new ResponseEntity<>(ResponseUtil.error(response), HttpStatus.BAD_REQUEST);
    }

    // miscellaneous exceptions
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<GenericResponse<?>> apiExceptionHandler(ApiException ex) {
        return new ResponseEntity<>(ResponseUtil.error(ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

}
