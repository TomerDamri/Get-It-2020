package GetIt.service;

import GetIt.exceptions.EmptyExpressionException;
import GetIt.exceptions.InternalServerErrorException;
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
import java.util.stream.Collectors;

@Service
public class TyposService {

    private static final String DELIMETER = " ";
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

    public List<String> getTyposV2(String youtubeUrl, Map<Integer, String> transcript, String expression) {
        if (expression.isEmpty()) {
            throw new EmptyExpressionException("You have to request a non-empty expression");
        }

        Set<String> dictionaryWithTranscript = getDictionaryWithTranscript(youtubeUrl, transcript);
        return getTyposFromDictionary(youtubeUrl, expression, dictionaryWithTranscript);
    }

    private List<String> getTyposFromDictionary(String youtubeUrl, String expression, Set<String> dictionaryWithTranscript) {
        boolean wasFoundTypo = false;

        List<String> words = Arrays.asList(expression.toLowerCase().split(DELIMETER));
        List<StringBuilder> returnTypos = Arrays.stream(new StringBuilder[suggestionsNumber]).map(stringBuilder -> new StringBuilder()).collect(Collectors.toList());

        Iterator<String> iterator = words.iterator();
        while (iterator.hasNext()) {
            String currWord = iterator.next();
            boolean isLastWordInExpression = !iterator.hasNext();

            if (!dictionaryWithTranscript.contains(currWord)) {
                //the creation of the dictionary file will be created once for all typos
                createDictionaryFile(youtubeUrl, dictionaryWithTranscript);
                wasFoundTypo = tryHandleTypo(isLastWordInExpression, currWord, returnTypos);
            } else {
                returnTypos = handleValidWord(returnTypos, currWord, isLastWordInExpression);
            }
        }

        return wasFoundTypo ? returnTypos.stream().map(StringBuilder::toString).collect(Collectors.toList()) : new ArrayList<>();
    }

    private List<StringBuilder> handleValidWord(List<StringBuilder> returnTypos, String currWord, boolean isLastWordInExpression) {
        String wordToAppend = prepareWordToAppending(isLastWordInExpression, currWord);
        returnTypos = returnTypos.stream().map(builder -> builder.append(wordToAppend)).collect(Collectors.toList());
        return returnTypos;
    }

    private boolean tryHandleTypo(boolean isLastWordInExpression, String word, List<StringBuilder> returnTypos) {
        boolean wasFoundTypo = false;

        List<String> typos = getTyposFromDictionary(word);
        if (typos.size() > 0) {
            appendFixedWordsToSuggestions(isLastWordInExpression, returnTypos, typos);
            wasFoundTypo = true;
        } else {
            // in case the "getTyposFromDictionary" don't return any suggestion word
            handleValidWord(returnTypos, word, isLastWordInExpression);
        }

        return wasFoundTypo;
    }

    private void appendFixedWordsToSuggestions(boolean isLastWordInExpression, List<StringBuilder> returnTypos, List<String> typos) {
        String wordToAppend;
        for (int i = 0; i < typos.size(); i++) {
            wordToAppend = prepareWordToAppending(isLastWordInExpression, typos.get(i));
            returnTypos.get(i).append(wordToAppend);
        }
        // if the typos (size < suggestionsNumber) we need to append the suggestion word to the remaining stringBuilders
        appendToRemainingSuggestionsTheFixedWord(isLastWordInExpression, returnTypos, typos);
    }

    private void appendToRemainingSuggestionsTheFixedWord(boolean isLastWordInExpression, List<StringBuilder> returnTypos, List<String> typos) {
        String wordToAppend;
        if (typos.size() < suggestionsNumber) {
            wordToAppend = prepareWordToAppending(isLastWordInExpression, typos.get(0));
            for (int i = typos.size(); i < suggestionsNumber; i++) {
                returnTypos.get(i).append(wordToAppend);
            }
        }
    }

    private String prepareWordToAppending(boolean isLastWordInExpression, String wordToAppend) {
        String str = wordToAppend;
        if (!isLastWordInExpression) {
            str += DELIMETER;
        }
        return str;
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
        } catch (FileNotFoundException ex) {
            String errorMessage = "Failed to find the original dictionary file.";
            LOGGER.warning(errorMessage);
            throw new InternalServerErrorException(errorMessage, ex);
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

    private List<String> getTyposFromDictionary(String word) {
        try {
            File dictionaryFile = new File(dictionaryWithTranscriptPath);
            File dir = new File("c:/spellchecker/");
            Directory directory = FSDirectory.open(dir.toPath());

            SpellChecker spellChecker = new SpellChecker(directory);

            spellChecker.indexDictionary(
                    new PlainTextDictionary(dictionaryFile.toPath()), new IndexWriterConfig(), false);

            return Arrays.asList(spellChecker.
                    suggestSimilar(word, suggestionsNumber));
        } catch (IOException ex) {
            throw new RuntimeException("internalServerError");
        }

    }

    private void createDictionaryFile(String youtubeUrl, Set<String> dictionaryWithTranscriptAsSet) {
        try {
            if (!youtubeUrl.equals(lastYoutubeUrl)) {
                File newDictionaryFile = new File(dictionaryWithTranscriptPath);
                writeDictionaryToFile(newDictionaryFile, dictionaryWithTranscriptAsSet);
                lastYoutubeUrl = youtubeUrl;
            }
        } catch (IOException ex) {
            String errorMessage = "Failed writing to new dictionary file.";
            LOGGER.warning(errorMessage);
            throw new InternalServerErrorException(errorMessage, ex);
        }
    }

    private void writeDictionaryToFile(File newDictionaryFile, Set<String> dictionary) throws IOException {
        //convert to list in order to sort the dictionary (ABC order)
        dictionary = new TreeSet<>(dictionary);
        FileOutputStream outputStream = new FileOutputStream(newDictionaryFile);
        for (String dictionaryWord : dictionary) {
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
