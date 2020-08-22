package GetIt.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR, reason = "An unexpected error occur in server.\nThe problem is related to fetching the transcript.")
public class PythonScriptException extends RuntimeException {

    public PythonScriptException(Throwable cause) {
        super(String.format("An unexpected error occur in server.\nThe problem is related to fetching the transcript.\nThe error message: %s", cause.getMessage()));
    }
}