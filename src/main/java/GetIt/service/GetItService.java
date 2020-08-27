package GetIt.service;

import GetIt.exceptions.EmptyExpressionException;
import GetIt.exceptions.EmptyYoutubeUrlException;
import GetIt.exceptions.InvalidTimeSlotException;
import GetIt.model.response.GetOccurrencesResponse;
import GetIt.model.response.GetTranscriptResponse;
import GetIt.model.response.GetTyposResponse;
import org.springframework.beans.factory.annotation.Autowired;

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
        validateYoutubeUrl(youtubeUrl);

        Map<Integer, String> transcript = transcriptService.getTranscript(youtubeUrl);

        LOGGER.info("Finish the 'getTranscript' Operation");
        return new GetTranscriptResponse(transcript);
    }

    public GetOccurrencesResponse getOccurrences(String expression, String youtubeUrl) {
        LOGGER.info("Start the 'getOccurrences' Operation");

        validateInputForGetOccurrences(expression, youtubeUrl);

        Map<Integer, String> transcript = transcriptService.getTranscript(youtubeUrl);
        List<Integer> occurrences = occurrencesService.getOccurrencesInTranscriptV2(transcript, expression.toLowerCase());

        LOGGER.info("finish the 'getOccurrences' operation");
        return new GetOccurrencesResponse(occurrences);
    }


    public GetTyposResponse getTypos(String expression, String youtubeUrl) {
        LOGGER.info("Start the 'getTypos' Operation");

        validateInputForGetTypos(expression, youtubeUrl);

        Map<Integer, String> transcript = transcriptService.getTranscript(youtubeUrl);
        List<String> typos = typosService.getTyposV2(youtubeUrl, transcript, expression);

        LOGGER.info("finish the 'getTypos' operation");
        return new GetTyposResponse(typos);
    }


    public void updateTranscript(String youtubeUrl, Integer timeSlots, String oldSentence, String fixedSentence) {
        LOGGER.info("Start the 'updateTranscript' Operation");

        validateInputForUpdateTranscript(youtubeUrl, timeSlots, oldSentence, fixedSentence);
        transcriptService.updateTranscript(youtubeUrl, timeSlots, oldSentence, fixedSentence);

        LOGGER.info("finish the 'updateTranscript' operation");
    }

    private void validateYoutubeUrl(String youtubeUrl) {
        if (isEmpty(youtubeUrl)) {
            throw new EmptyYoutubeUrlException("You have to request a non-empty youtube url");
        }
    }

    private void validateInputForGetTypos(String expression, String youtubeUrl) {
        validateBaseRequest(expression, youtubeUrl);
    }

    private void validateInputForGetOccurrences(String expression, String youtubeUrl) {
        validateBaseRequest(expression, youtubeUrl);
    }

    private void validateInputForUpdateTranscript(String youtubeUrl, Integer timeSlots, String oldSentence, String fixedSentence) {
        validateBaseRequest(oldSentence, youtubeUrl);

        if (timeSlots == null || timeSlots < 0) {
            throw new InvalidTimeSlotException("You have to request a positive time slot");
        }
        validateExpression(fixedSentence);
    }


    private void validateBaseRequest(String expression, String youtubeUrl) {
        validateYoutubeUrl(youtubeUrl);
        validateExpression(expression);
    }


    private void validateExpression(String expression) {
        if (isEmpty(expression)) {
            throw new EmptyExpressionException("You have to request a non-empty expression");
        }
    }

    private boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }
}
