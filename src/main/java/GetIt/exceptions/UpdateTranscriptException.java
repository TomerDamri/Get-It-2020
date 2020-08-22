package GetIt.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "Failed to update the transcript.\n There is not a sentence like you requested at the time slot you requested in this transcript.")
public class UpdateTranscriptException extends RuntimeException {
    public UpdateTranscriptException(String message) {
        super(message);
    }
}