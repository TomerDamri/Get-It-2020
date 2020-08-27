package GetIt.exceptions;

public class InternalServerErrorException extends RuntimeException {

    public InternalServerErrorException(String message, Throwable cause) {
        super(String.format("An unexpected error occur in server.\n%s\nThe error message: %s", message, cause.getMessage()));
    }
}