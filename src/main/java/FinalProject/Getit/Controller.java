package FinalProject.Getit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path = "/Ctrl_F_App")
public class Controller {

    @Autowired
    private Service service;

    @GetMapping(consumes = "application/json", produces = "application/json")
    public Map<String, String> findAllByKeywords(@RequestBody SearchDataRequest searchDataRequest) {
        return service.getOccurrences(searchDataRequest.getKeyword(), searchDataRequest.getYoutubeUrl());
    }

    @GetMapping("/word/{word}/suggestions")
    public List<String> getWordSuggestions(@PathVariable String word) throws IOException {
        return service.getWordSuggestions(word);
    }
}
