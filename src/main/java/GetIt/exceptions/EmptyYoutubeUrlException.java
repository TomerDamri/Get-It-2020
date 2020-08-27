package GetIt.exceptions;

public class EmptyYoutubeUrlException extends RuntimeException {
    public EmptyYoutubeUrlException(String message) {
        super(message);
    }
}