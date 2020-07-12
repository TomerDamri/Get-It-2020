package GetIt.service;

import GetIt.model.response.GetOccurrencesResponse;
import GetIt.model.response.GetTyposResponse;
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
import java.util.logging.Logger;

@org.springframework.stereotype.Service
public class GetItService {

    private final static Logger LOGGER = Logger.getLogger(Logger.class.getName());
    private static final String userDirectory = Paths.get("")
            .toAbsolutePath()
            .toString();
    private static final YoutubeTranscriptService YOUTUBE_TRANSCRIPT_SERVICE = new YoutubeTranscriptService();
    private static final String dictionaryWithTranscriptPath = userDirectory + "/scripts/newDictionary.txt";

    private static final int suggestionsNumber = 3;

    List<String> dictionaryWithTranscript;

    Map<String, String> transcript;

    String transcribedYoutubeUrl;

    public GetOccurrencesResponse getOccurrences(String word, String youtubeUrl) {
        LOGGER.info(String.format("getting occurrences from %s for %s", youtubeUrl, word));
        if (!youtubeUrl.equals( transcribedYoutubeUrl)) {
            transcript = YOUTUBE_TRANSCRIPT_SERVICE.getYoutubeTranscript(youtubeUrl);
            transcribedYoutubeUrl = youtubeUrl;
        }

        List<Integer> occurrences = getOccurrencesInTranscript(word);
        return new GetOccurrencesResponse(occurrences);
    }

    List<Integer> getOccurrencesInTranscript(String word) {
        List<Float> occurrencesAsFloat = new ArrayList<>();
        for (String key : transcript.keySet()) {
            if (key.contains(word)) {
                occurrencesAsFloat.add(Float.valueOf(transcript.get(key)));
            }
        }
        //use HashSet to avoid duplicates
        Set<Integer> occurrencesAsInteger = new HashSet<>();
        for (Float time : occurrencesAsFloat) {
            occurrencesAsInteger.add(time.intValue());
        }

        List<Integer> sortedOccurrences = new ArrayList<>(occurrencesAsInteger);
        Collections.sort(sortedOccurrences);
        return sortedOccurrences;
    }

    public GetTyposResponse getTypos(String word, String youtubeUrl) throws IOException {
        LOGGER.info(String.format("getting typos from %s for %s", youtubeUrl, word));
        if (!youtubeUrl.equals( transcribedYoutubeUrl)|| transcript == null || dictionaryWithTranscript == null) {
            transcript = YOUTUBE_TRANSCRIPT_SERVICE.getYoutubeTranscript(youtubeUrl);
            transcribedYoutubeUrl = youtubeUrl;
            createDictionaryWithTranscript();
        }
        List<String> typos = null;
        if (!dictionaryWithTranscript.contains(word)) {
            typos = getTyposFromDictionary(word);
        }
        return new GetTyposResponse(typos);
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

    private void createDictionaryWithTranscript() throws IOException { ;
        //using set in order to avoid duplicates
        Set<String> dictionaryAsSet = getOriginalDictionary();
        dictionaryAsSet.addAll(convertYoutubeTranscriptToWords(transcript.keySet()));
        //convert to list in order to sort the dictionary (ABC order)
        List<String> dictionaryAsList = new ArrayList<>(dictionaryAsSet);
        Collections.sort(dictionaryAsList);
        dictionaryWithTranscript = dictionaryAsList;

        File newDictionary = new File(dictionaryWithTranscriptPath);

        FileOutputStream outputStream = new FileOutputStream(newDictionary);
        for (String dictionaryWord : dictionaryWithTranscript) {
            dictionaryWord = dictionaryWord + '\n';
            byte[] strToBytes = dictionaryWord.getBytes();
            outputStream.write(strToBytes);
        }
        outputStream.close();
    }

    private Set<String> convertYoutubeTranscriptToWords(Set<String> youtubeTranscriptSentences) {
        Set<String> youtubeTranscriptWords = new HashSet<>();
        for (String sentence : youtubeTranscriptSentences) {
            youtubeTranscriptWords.addAll(Arrays.asList(sentence.split(" ")));
        }
        return youtubeTranscriptWords;
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

//    public List<String> getWordSuggestions(String word) throws IOException {
//
//        String dictPath = userDirectory + "/scripts/dictionary";
//
//        File dir = new File("c:/spellchecker/");
//
//        Directory directory = FSDirectory.open(dir.toPath());
//
//        File pathFile = new File(dictPath);
//
//        SpellChecker spellChecker = new SpellChecker(directory);
//
//        spellChecker.indexDictionary(
//                new PlainTextDictionary(pathFile.toPath()), new IndexWriterConfig(), false);
//
//        int suggestionsNumber = 10;
//
//        return Arrays.asList(spellChecker.
//                suggestSimilar(word, suggestionsNumber));
//    }

}
