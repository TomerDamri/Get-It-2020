package GetIt.exceptions;

public class PythonScriptException extends RuntimeException {

    public PythonScriptException(Throwable cause) {
        super(String.format("An unexpected error occur in server.\nThe problem is related to fetching the transcript.\nThe error message: %s", cause.getMessage()));
    }
}