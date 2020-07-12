package GetIt.controller;

import GetIt.model.response.GetOccurrencesResponse;
import GetIt.model.response.GetTyposResponse;
import GetIt.model.request.GetOccurrencesRequest;
import GetIt.model.request.GetTyposRequest;
import GetIt.service.GetItService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping(path = "/Ctrl_F_App")
public class GetItController {

    @Autowired
    private GetItService getItService;

    @PostMapping(consumes = "application/json", produces = "application/json")
    public GetOccurrencesResponse getOccurrences(@RequestBody GetOccurrencesRequest getOccurrencesRequest) {
        return getItService.getOccurrences(getOccurrencesRequest.getWord(), getOccurrencesRequest.getYoutubeUrl());
    }

    @PostMapping("/word/typos")
    public GetTyposResponse getWordTypos(@RequestBody GetTyposRequest getTyposRequest) throws IOException {
        return getItService.getTypos(getTyposRequest.getWord(), getTyposRequest.getYoutubeUrl());
    }
}
