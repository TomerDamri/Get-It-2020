package GetIt.exceptions;

public class PythonScriptException extends RuntimeException {

    public PythonScriptException(Throwable cause) {
        super(String.format("An unexpected error occurred in server.\nThe problem is related to fetching the transcript from youtube.\nThe error message: %s", cause.getMessage()));
    }
}