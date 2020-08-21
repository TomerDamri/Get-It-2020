package GetIt.exceptions.base;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
public class InternalServerErrorException extends RuntimeException {

    public InternalServerErrorException(Throwable cause) {
        super(String.format("An error occur in server.\nThe error message: %s", cause.getMessage()));
    }
}