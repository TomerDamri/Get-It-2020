package GetIt.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR, reason = "An unexpected error occur in server")
public class InternalServerErrorException extends RuntimeException {

    public InternalServerErrorException(String message, Throwable cause) {
        super(String.format("An unexpected error occur in server.\n%s\nThe error message: %s",message, cause.getMessage()));
    }
}