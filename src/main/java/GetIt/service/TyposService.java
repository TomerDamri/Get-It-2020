package GetIt.service;

import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.spell.PlainTextDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;

@Service
public class TyposService {

    public static final String DELIMETER = " ";
    private final static Logger LOGGER = Logger.getLogger(Logger.class.getName());
    private static final String userDirectory = Paths.get("")
            .toAbsolutePath()
            .toString();
    private static final String dictionaryWithTranscriptPath = (userDirectory.contains("\\")) ? userDirectory + "\\scripts\\newDictionary.txt" : userDirectory + "/scripts/newDictionary.txt";
    private static final String dictionaryWithoutTranscriptPath = (userDirectory.contains("\\")) ? userDirectory + "\\scripts\\dictionary" : userDirectory + "/scripts/dictionary";
    private static final int suggestionsNumber = 3;


    public List<String> getTyposV2(Map<Integer, String> transcript, String word) throws IOException {
        List<String> dictionaryWithTranscript = createDictionaryWithTranscriptV2(transcript);

        List<String> typos = new ArrayList<>();
        if (!dictionaryWithTranscript.contains(word.toLowerCase())) {
            typos = getTyposFromDictionary(word.toLowerCase());
        }

        return typos;
    }

    private List<String> createDictionaryWithTranscriptV2(Map<Integer, String> transcript) throws IOException {
        List<String> dictionaryWithTranscript;

        //using set in order to avoid duplicates
        Set<String> dictionaryAsSet = getOriginalDictionary();
        dictionaryAsSet.addAll(convertYoutubeTranscriptToWords(transcript.values()));
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

        return dictionaryWithTranscript;
    }

    private Set<String> getOriginalDictionary() throws FileNotFoundException {
        File pathFile = new File(dictionaryWithoutTranscriptPath);
        Scanner s = new Scanner(pathFile);
        Set<String> dictionary = new HashSet<>();
        while (s.hasNext()) {
            dictionary.add(s.next());
        }
        s.close();
        return dictionary;
    }


    private Set<String> convertYoutubeTranscriptToWords(Collection<String> youtubeTranscriptSentences) {
        Set<String> youtubeTranscriptWords = new HashSet<>();
        for (String sentence : youtubeTranscriptSentences) {
            youtubeTranscriptWords.addAll(Arrays.asList(sentence.split(DELIMETER)));
        }
        return youtubeTranscriptWords;
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
}
