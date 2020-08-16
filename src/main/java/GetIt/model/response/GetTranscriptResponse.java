package GetIt.model.response;

import java.util.Map;

public class GetTranscriptResponse {
    private final Map<Integer, String> transcript;

    public GetTranscriptResponse(Map<Integer, String> transcript) {
        this.transcript = transcript;
    }

    public final Map<Integer, String> getTranscript() {
        return transcript;
    }
}
