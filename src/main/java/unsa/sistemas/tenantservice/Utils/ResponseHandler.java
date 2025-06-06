package unsa.sistemas.tenantservice.Utils;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ResponseHandler {
    public static <T> ResponseEntity<ResponseWrapper<T>> generateResponse(String message, HttpStatus status, T responseObj) {
        ResponseWrapper<T> response = new ResponseWrapper<>(message, responseObj);
        return new ResponseEntity<>(response, status);
    }
}
