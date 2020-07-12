package GetIt.controller;

import GetIt.service.GetItService;
import GetIt.Request.GetOccurrencesRequest;
import GetIt.Request.GetTyposRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping(path = "/Ctrl_F_App")
public class GetItController {

    @Autowired
    private GetItService getItService;


    @CrossOrigin
    @PostMapping(consumes = "application/json", produces = "application/json")
    public List<Float> getOccurrences(@RequestBody GetOccurrencesRequest getOccurrencesRequest) {
        return getItService.getOccurrences(getOccurrencesRequest.getWord(), getOccurrencesRequest.getYoutubeUrl());
    }

    @CrossOrigin
    @PostMapping("/word/typos")
    public List<String> getWordTypos(@RequestBody GetTyposRequest getTyposRequest) throws IOException {
        return getItService.getTypos(getTyposRequest.getWord(), getTyposRequest.getYoutubeUrl());
    }
}
