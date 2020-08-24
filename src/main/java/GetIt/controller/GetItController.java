package GetIt.controller;

import GetIt.model.request.GetOccurrencesRequest;
import GetIt.model.request.GetTranscriptRequest;
import GetIt.model.request.GetTyposRequest;
import GetIt.model.request.UpdateTranscriptRequest;
import GetIt.model.response.GetOccurrencesResponse;
import GetIt.model.response.GetTranscriptResponse;
import GetIt.model.response.GetTyposResponse;
import GetIt.service.GetItService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/Ctrl_F_App")
public class GetItController {

    @Autowired
    private GetItService getItService;

    @PostMapping(path = "/transcript", consumes = "application/json", produces = "application/json")
    public GetTranscriptResponse getTranscript(@RequestBody GetTranscriptRequest getTranscriptRequest) {
        return getItService.getTranscript(getTranscriptRequest.getYoutubeUrl());
    }

    @PostMapping(consumes = "application/json", produces = "application/json")
    public GetOccurrencesResponse getOccurrences(@RequestBody GetOccurrencesRequest getOccurrencesRequest) {
        return getItService.getOccurrences(getOccurrencesRequest.getWord(), getOccurrencesRequest.getYoutubeUrl());
    }

    @PostMapping("/word/typos")
    public GetTyposResponse getWordTypos(@RequestBody GetTyposRequest getTyposRequest) {
        return getItService.getTypos(getTyposRequest.getWord(), getTyposRequest.getYoutubeUrl());
    }

    @PostMapping("/transcript/update")
    public void updateTranscript(@RequestBody UpdateTranscriptRequest updateTranscriptRequest) {
        getItService.updateTranscript(updateTranscriptRequest.getYoutubeUrl(), updateTranscriptRequest.getTimeSlots(), updateTranscriptRequest.getOldSentence(), updateTranscriptRequest.getFixedSentence());
    }

    @GetMapping("/home")
    public ResponseEntity<Void> keepAlive() {
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
