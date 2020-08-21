package GetIt.service;

import GetIt.model.repositoriesModels.DictionaryEntity;
import GetIt.repositories.DictionariesRepository;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.spell.PlainTextDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
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
    private String lastYoutubeUrl;
    private Map<String, Long> youtubeUrlToId = new HashMap<>();

    @Autowired
    private DictionariesRepository dictionariesRepository;

    public List<String> getTyposV2(String youtubeUrl, Map<Integer, String> transcript, String word) throws IOException {
        String fixedWord = word.toLowerCase();
        Set<String> dictionaryWithTranscript = getDictionaryWithTranscript(youtubeUrl, transcript);

        List<String> typos = new ArrayList<>();
        if (!dictionaryWithTranscript.contains(fixedWord)) {
            typos = getTyposFromDictionary(youtubeUrl, dictionaryWithTranscript, fixedWord);
        }

        return typos;
    }

    private Set<String> getDictionaryWithTranscript(String youtubeUrl, Map<Integer, String> transcript) {
        DictionaryEntity dictionaryFromRepository = getDictionaryFromRepository(youtubeUrl);

        return (dictionaryFromRepository != null) ? dictionaryFromRepository.getDictionary() : createDictionaryWithTranscriptV2(youtubeUrl, transcript);
    }

    private Set<String> createDictionaryWithTranscriptV2(String youtubeUrl, Map<Integer, String> transcript) {
        try {
            //using set in order to avoid duplicates
            Set<String> dictionaryAsSet = getOriginalDictionary();
            dictionaryAsSet.addAll(convertYoutubeTranscriptToWords(transcript.values()));

            new Thread(() -> saveDictionaryInRepository(youtubeUrl, dictionaryAsSet)).start();
            return dictionaryAsSet;
        } catch (IOException ex) {
            // TODO: 21/08/2020
            throw new RuntimeException();
        }
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

    private List<String> getTyposFromDictionary(String youtubeUrl, Set<String> dictionaryWithTranscript, String word) throws IOException {
        File dir = new File("c:/spellchecker/");
        File dictionaryFile = new File(dictionaryWithTranscriptPath);

        Directory directory = FSDirectory.open(dir.toPath());

        createDictionaryFile(youtubeUrl, dictionaryWithTranscript, dictionaryFile);

        SpellChecker spellChecker = new SpellChecker(directory);

        spellChecker.indexDictionary(
                new PlainTextDictionary(dictionaryFile.toPath()), new IndexWriterConfig(), false);

        return Arrays.asList(spellChecker.
                suggestSimilar(word, suggestionsNumber));

    }

    private void createDictionaryFile(String youtubeUrl, Set<String> dictionaryWithTranscriptAsSet, File newDictionaryFile) throws IOException {
        if (!youtubeUrl.equals(lastYoutubeUrl)) {
            //convert to list in order to sort the dictionary (ABC order)
            List<String> dictionaryAsList = new ArrayList<>(dictionaryWithTranscriptAsSet);
            Collections.sort(dictionaryAsList);

            writeDictionaryToFile(newDictionaryFile, dictionaryAsList);
            lastYoutubeUrl = youtubeUrl;
        }
    }

    private void writeDictionaryToFile(File newDictionaryFile, List<String> dictionaryAsList) throws IOException {
        FileOutputStream outputStream = new FileOutputStream(newDictionaryFile);
        for (String dictionaryWord : dictionaryAsList) {
            dictionaryWord = dictionaryWord + '\n';
            byte[] strToBytes = dictionaryWord.getBytes();
            outputStream.write(strToBytes);
        }
        outputStream.close();
    }

    @Cacheable(value = "dictionaries", key = "#youtubeUrl", cacheManager = "dictionariesCacheManager")
    private DictionaryEntity getDictionaryFromRepository(String youtubeUrl) {
        Long id = youtubeUrlToId.get(youtubeUrl);

        return (id != null) ? dictionariesRepository.findById(id).orElse(null) : null;
    }

    @Cacheable(value = "dictionaries", key = "#youtubeUrl", cacheManager = "dictionariesCacheManager")
    private DictionaryEntity saveDictionaryInRepository(String youtubeUrl, Set<String> newDictionary) {
        DictionaryEntity dictionaryEntity = new DictionaryEntity(newDictionary);
        DictionaryEntity save = dictionariesRepository.save(dictionaryEntity);
        youtubeUrlToId.put(youtubeUrl, save.getId());

        LOGGER.info("The dictionary saved successfully in dictionaryEntity repository");
        return save;
    }
}
