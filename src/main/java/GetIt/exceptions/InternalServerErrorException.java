package GetIt.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR, reason = "An unexpected error occur in server")
public class InternalServerErrorException extends RuntimeException {

    public InternalServerErrorException(Throwable cause) {
        super(String.format("An unexpected error occur in server.\nThe error message: %s", cause.getMessage()));
    }
}