package GetIt.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "You have to request a non-empty youtube url")
public class EmptyYoutubeUrlException extends RuntimeException {
    public EmptyYoutubeUrlException(String message) {
        super(message);
    }
}