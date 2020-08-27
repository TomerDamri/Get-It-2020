package GetIt.exceptions;


public class TranscriptNotFoundException extends RuntimeException {
    public TranscriptNotFoundException(String youtubeUrl) {
        super(String.format("There is no transcript for this video : '%s'", youtubeUrl));
    }
}