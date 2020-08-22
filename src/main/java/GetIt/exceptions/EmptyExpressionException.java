package GetIt.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "You have to request a non-empty expression")
public class EmptyExpressionException extends RuntimeException {
    public EmptyExpressionException(String message) {
        super(message);
    }
}