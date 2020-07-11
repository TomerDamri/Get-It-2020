package GetIt.service;

import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.spell.PlainTextDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

@org.springframework.stereotype.Service
public class GetItService {

    private static final String userDirectory = Paths.get("")
            .toAbsolutePath()
            .toString();
    private static final YoutubeTranscriptService YOUTUBE_TRANSCRIPT_SERVICE = new YoutubeTranscriptService();
    private static final String dictionaryWithTranscriptPath = userDirectory + "/scripts/newDictionary.txt";

    private static final int suggestionsNumber = 10;

    public List<Float> getOccurrences(String keyword, String youtubeUrl) {
        String path = userDirectory + "/scripts/youtube_api.py";
        Map<String, String> map = YOUTUBE_TRANSCRIPT_SERVICE.getYoutubeTranscript(youtubeUrl);
        List<Float> captions = new ArrayList<>();
        for (String key : map.keySet()) {
            if (key.contains(keyword)) {
                captions.add(Float.valueOf(map.get(key)));
            }
        }
        Collections.sort(captions);
        return captions;
    }

    public List<String> getTypos(String word, String youtubeUrl) throws IOException {

        createDictionaryWithTranscript(youtubeUrl);

        return getTyposFromDictionary(word);
    }

    private List<String> getTyposFromDictionary(String word) throws IOException {
        File dir = new File("c:/spellchecker/");

        Directory directory = FSDirectory.open(dir.toPath());

        File pathFile = new File(dictionaryWithTranscriptPath);

        SpellChecker spellChecker = new SpellChecker(directory);

        spellChecker.indexDictionary(
                new PlainTextDictionary(pathFile.toPath()), new IndexWriterConfig(), false);

        return Arrays.asList(spellChecker.
                suggestSimilar(word, suggestionsNumber));

    }

    private void createDictionaryWithTranscript(String youtubeUrl) throws IOException {
        List<String> dictionaryWithTranscript = getDictionaryWithTranscript(youtubeUrl);
        File newDictionary = new File(dictionaryWithTranscriptPath);

        FileOutputStream outputStream = new FileOutputStream(newDictionary);
        for (String dictionaryWord : dictionaryWithTranscript) {
            dictionaryWord = dictionaryWord + '\n';
            byte[] strToBytes = dictionaryWord.getBytes();
            outputStream.write(strToBytes);
        }
        outputStream.close();
    }

    private List<String> getDictionaryWithTranscript(String youtubeUrl) throws FileNotFoundException {
        //using set in order to avoid duplicates
        Set<String> dictionaryAsSet = getOriginalDictionary();
        String path = userDirectory + "/scripts/youtube_api.py";
        Map<String, String> youtubeTranscript = YOUTUBE_TRANSCRIPT_SERVICE.getYoutubeTranscript(youtubeUrl);
        dictionaryAsSet.addAll(youtubeTranscript.keySet());
        //convert to list in order to sort the dictionary (ABC order)
        List<String> dictionaryAsList = new ArrayList<>(dictionaryAsSet);
        Collections.sort(dictionaryAsList);
        return dictionaryAsList;
    }

    private Set<String> getOriginalDictionary() throws FileNotFoundException {
        String dictionaryPath = userDirectory + "/scripts/dictionary";
        File pathFile = new File(dictionaryPath);
        Scanner s = new Scanner(pathFile);
        Set<String> dictionary = new HashSet<>();
        while (s.hasNext()) {
            dictionary.add(s.next());
        }
        s.close();
        return dictionary;
    }

    public List<String> getWordSuggestions(String word) throws IOException {

        String dictPath = userDirectory + "/scripts/dictionary";

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

}
