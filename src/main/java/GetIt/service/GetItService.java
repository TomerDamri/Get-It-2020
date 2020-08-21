package GetIt.service;

import GetIt.model.response.GetOccurrencesResponse;
import GetIt.model.response.GetTranscriptResponse;
import GetIt.model.response.GetTyposResponse;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@org.springframework.stereotype.Service
public class GetItService {

    private final static Logger LOGGER = Logger.getLogger(Logger.class.getName());

    @Autowired
    private TranscriptService transcriptService;
    @Autowired
    private OccurrencesService occurrencesService;
    @Autowired
    private TyposService typosService;

    public GetTranscriptResponse getTranscript(String youtubeUrl) {
        LOGGER.info("Start the 'getTranscript' Operation");

        Map<Integer, String> transcript = transcriptService.getTranscript(youtubeUrl);

        LOGGER.info("Finish the 'getTranscript' Operation");
        return new GetTranscriptResponse(transcript);
    }

    public GetOccurrencesResponse getOccurrences(String word, String youtubeUrl) {
        LOGGER.info("Start the 'getOccurrences' Operation");

        Map<Integer, String> transcript = transcriptService.getTranscript(youtubeUrl);
        List<Integer> occurrences = occurrencesService.getOccurrencesInTranscriptV2(transcript, word.toLowerCase());

        LOGGER.info("finish the 'getOccurrences' operation");
        return new GetOccurrencesResponse(occurrences);
    }

    public GetTyposResponse getTypos(String word, String youtubeUrl) throws IOException {
        LOGGER.info("Start the 'getTypos' Operation");

        Map<Integer, String> transcript = transcriptService.getTranscript(youtubeUrl);
        List<String> typos = typosService.getTyposV2(youtubeUrl, transcript, word);

        LOGGER.info("finish the 'getTypos' operation");
        return new GetTyposResponse(typos);
    }

    public void updateTranscript(String youtubeUrl, Integer timeSlots, String oldSentence, String fixedSentence) {
        LOGGER.info("Start the 'updateTranscript' Operation");

        transcriptService.updateTranscript(youtubeUrl, timeSlots, oldSentence, fixedSentence);

        LOGGER.info("finish the 'updateTranscript' operation");
    }
}
