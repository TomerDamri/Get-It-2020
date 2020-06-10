package FinalProject.Getit;

import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.spell.PlainTextDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@org.springframework.stereotype.Service
public class Service {

    public Map<String, String> getOccurrences(String keyword, String youtubeUrl) {
        PythonScriptsExecutor pythonScriptsExecutor = new PythonScriptsExecutor();
        String userDirectory = getUserDirectory();

        String path = userDirectory + "\\scripts\\youtube_api.py";
        return pythonScriptsExecutor.runScript(path, youtubeUrl, keyword);
    }

    public List<String> getWordSuggestions(String word) throws IOException {

        String userDirectory = getUserDirectory();

        String dictPath = userDirectory + "\\scripts\\dictionary";

        File dir = new File("c:/spellchecker/");

        Directory directory = FSDirectory.open(dir.toPath());

        File pathFile = new File(dictPath);

        SpellChecker spellChecker = new SpellChecker(directory);

        spellChecker.indexDictionary(
                new PlainTextDictionary(pathFile.toPath()), new IndexWriterConfig(), false);

        int suggestionsNumber = 10;

        return Arrays.asList(spellChecker.
                suggestSimilar(word, suggestionsNumber));
    }

    private String getUserDirectory() {
        return Paths.get("")
                .toAbsolutePath()
                .toString();
    }

}
