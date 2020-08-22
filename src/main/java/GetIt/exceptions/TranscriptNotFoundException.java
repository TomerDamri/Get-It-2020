package GetIt.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "There is no transcript for this video")
public class TranscriptNotFoundException extends RuntimeException {
    public TranscriptNotFoundException(String message) {
        super(message);
    }
}