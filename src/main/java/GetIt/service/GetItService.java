package GetIt.service;

import GetIt.model.request.GetOccurrencesRequest;
import GetIt.model.request.GetTranscriptRequest;
import GetIt.model.request.GetTyposRequest;
import GetIt.model.request.UpdateTranscriptRequest;
import GetIt.model.response.GetOccurrencesResponse;
import GetIt.model.response.GetTranscriptResponse;
import GetIt.model.response.GetTyposResponse;
import GetIt.validator.GetItValidator;
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
    @Autowired
    private GetItValidator validator;

    public GetTranscriptResponse getTranscript(GetTranscriptRequest request) {
        LOGGER.info("Started getting transcript");
        validator.validateGetTranscriptRequest(request);
        Map<Integer, String> transcript = transcriptService.getTranscript(request.getYoutubeUrl());
        LOGGER.info("Done getting transcript");
        return new GetTranscriptResponse(transcript);
    }

    public GetOccurrencesResponse getOccurrences(GetOccurrencesRequest request) {
        LOGGER.info("Started getting occurrences");
        validator.validateGetOccurrencesRequest(request);

        Map<Integer, String> transcript = transcriptService.getTranscript(request.getYoutubeUrl());
        List<Integer> occurrences = occurrencesService.getOccurrencesInTranscript(transcript, request.getExpression().toLowerCase());

        LOGGER.info("Done getting occurrences");
        return new GetOccurrencesResponse(occurrences);
    }

    public GetTyposResponse getTypos(GetTyposRequest request) {
        LOGGER.info("Started getting typos");
        validator.validateGetTyposRequest(request);

        Map<Integer, String> transcript = transcriptService.getTranscript(request.getYoutubeUrl());
        List<String> typos = typosService.getTypos(request.getYoutubeUrl(), transcript, request.getExpression());

        LOGGER.info("Done getting typos");
        return new GetTyposResponse(typos);
    }

    public void updateTranscript(UpdateTranscriptRequest request) {
        LOGGER.info("Started updating transcript");

        validator.validateUpdateTranscriptRequest(request);
        transcriptService.updateTranscript(request.getYoutubeUrl(), request.getTimeSlot(), request.getExpression(), request.getFixedExpression());

        LOGGER.info("Done updating transcript");
    }
}




