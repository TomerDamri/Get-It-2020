package GetIt.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "You have to request a positive time slot")
public class NegativeTimeSlotException extends RuntimeException {
    public NegativeTimeSlotException(String message) {
        super(message);
    }
}