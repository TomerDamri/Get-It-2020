package GetIt.service;

import GetIt.model.response.GetOccurrencesResponse;
import GetIt.model.response.GetTranscriptResponse;
import GetIt.model.response.GetTyposResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
public class GetItService {

    private final static Logger LOGGER = Logger.getLogger(Logger.class.getName());
    private static final String userDirectory = Paths.get("")
            .toAbsolutePath()
            .toString();
    private static final YoutubeTranscriptService YOUTUBE_TRANSCRIPT_SERVICE = new YoutubeTranscriptService();
    private static final String dictionaryWithTranscriptPath = (userDirectory.contains("\\")) ? userDirectory + "\\scripts\\newDictionary.txt" : userDirectory + "/scripts/newDictionary.txt";
    private static final String dictionaryWithoutTranscriptPath = (userDirectory.contains("\\")) ? userDirectory + "\\scripts\\dictionary" : userDirectory + "/scripts/dictionary";
    private static final String customTranscriptsDirectoryPath = (userDirectory.contains("\\")) ? userDirectory + "\\customTranscripts" : userDirectory + "/customTranscripts";


    private static final int suggestionsNumber = 3;
    public static final String DELIMETER = " ";
    public static final Integer MAX_DIFF = 5;
    public static final Integer MIN_DIFF = 3;

    private List<String> dictionaryWithTranscript;
    private Map<String, String> transcript;
    private Map<Integer, String> transcriptV2;
    private String transcribedYoutubeUrl;
    private ObjectMapper objectMapper = new ObjectMapper();

    public void updateTranscript(String youtubeUrl, Integer timeSlots, String oldSentence, String fixedSentence) {
        LOGGER.info(String.format("updating the transcript of the %s youtube video", youtubeUrl));
        createTranscript(youtubeUrl);
        LOGGER.info("transcript created successfully");
        putNewSentenceInTranscript(timeSlots, oldSentence, fixedSentence);
        LOGGER.info("transcript object updated successfully");
        saveTranscriptAsFile(youtubeUrl);
        LOGGER.info(String.format("Finish updating the transcript of the %s youtube video", youtubeUrl));
    }

    private void saveTranscriptAsFile(String youtubeUrl) {
        try {
            File newCustomTranscriptFile = new File(getCustomTranscriptFileName(youtubeUrl));
            objectMapper.writeValue(newCustomTranscriptFile, transcriptV2);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update the transcript.\n An unexpected error occur while saving the transcript to file.");
        }
    }

    private String getCustomTranscriptFileName(String youtubeUrl) {
        String fileNameTemplate = (userDirectory.contains("\\")) ? "\\%s.json" : "/%s.json";
        String[] split = youtubeUrl.split("v=");
        String videoId = split.length == 2 ? split[1] : youtubeUrl;
        return new StringBuilder(customTranscriptsDirectoryPath).append(String.format(fileNameTemplate, videoId)).toString();
    }

    private void putNewSentenceInTranscript(Integer timeSlots, String oldSentence, String fixedSentence) {
        if (!transcriptV2.containsKey(timeSlots) || !transcriptV2.get(timeSlots).equals(oldSentence)) {
            throw new RuntimeException(String.format("Failed to update the transcript.\n There is not a sentence like %s in the transcript.", oldSentence));
        }
        transcriptV2.put(timeSlots, fixedSentence);
    }

    public GetOccurrencesResponse getOccurrencesV2(String word, String youtubeUrl) {
        LOGGER.info(String.format("getting occurrences from %s for %s", youtubeUrl, word));

        createTranscript(youtubeUrl);
        List<Integer> occurrences = getOccurrencesInTranscriptV2(word.toLowerCase());
        LOGGER.info("finish the 'getOccurrences' operation");
        return new GetOccurrencesResponse(occurrences);
    }

    public GetTranscriptResponse getTranscript2(String youtubeUrl) {
        LOGGER.info("Start the  'getTranscriptV2' Operation");
        createTranscript(youtubeUrl);
        LOGGER.info("Finish the 'getTranscriptV2' Operation");
        return new GetTranscriptResponse(transcriptV2);
    }


    public void createTranscript(String youtubeUrl) {
        if (!youtubeUrl.equals(transcribedYoutubeUrl)) {
            transcriptV2 = isTranscriptFileExist(youtubeUrl) ? getTranscriptFromFile(youtubeUrl) :
                    YOUTUBE_TRANSCRIPT_SERVICE.getYoutubeTranscriptV2(youtubeUrl);
            transcribedYoutubeUrl = youtubeUrl;
        }
    }

    private boolean isTranscriptFileExist(String youtubeUrl) {
        File file = new File(getCustomTranscriptFileName(youtubeUrl));
        return file.exists();
    }

    private Map<Integer, String> getTranscriptFromFile(String youtubeUrl) {
        Map<Integer, String> transcript;
        try {
            File transcriptFile = new File(getCustomTranscriptFileName(youtubeUrl));
            transcript = objectMapper.readValue(transcriptFile, new TypeReference<Map<Integer, String>>() {
            });
        } catch (Exception e) {
            LOGGER.info("Failed reading the transcript from file.\n An unexpected error occur while reading the transcript FROM file.\nWill be calculated from from scratch.");
            transcript = YOUTUBE_TRANSCRIPT_SERVICE.getYoutubeTranscriptV2(youtubeUrl);
        }

        return transcript;
    }


    private List<Integer> getOccurrencesInTranscriptV2(String word) {
        List<Integer> allOccurrences;
        List<String> words = Arrays.asList(word.split(DELIMETER));

        if (words.size() > 1) {
            allOccurrences = getExpressionOccurrences(words);
        } else {
            allOccurrences = getWordOccurrences(word);
        }

        return filterCloseTimeSlotsFromAllOccurrences(allOccurrences);
    }

    private List<Integer> filterCloseTimeSlotsFromAllOccurrences(List<Integer> allOccurrences) {
        Collections.sort(allOccurrences);
        List<Integer> filteredList = new LinkedList<>();
        Integer prevTime = null;
        boolean isHasToRemove;

        for (Integer currTime : allOccurrences) {
            isHasToRemove = false;
            if (prevTime == null) {
                prevTime = currTime;
            } else {
                if (currTime - prevTime > MIN_DIFF) {
                    prevTime = currTime;
                } else {
                    isHasToRemove = true;
                }
            }

            if (!isHasToRemove) {
                filteredList.add(currTime);
            }
        }

        return filteredList;
    }

    //create hashMap of : key = time slot, value = list of words form desired words list that are included in the "key" time slot
    private Map<Integer, List<String>> createWordsOccurrencesMap(List<String> words) {
        Map<Integer, List<String>> searchWordOccurrences = new HashMap<>();

        for (Map.Entry<Integer, String> entry : transcriptV2.entrySet()) {
            for (String word : words) {
                if (entry.getValue().contains(word)) {
                    addWordToWordOccurrencesMap(searchWordOccurrences, entry, word);
                }
            }
        }

        return searchWordOccurrences;
    }

    //It is not a simple put, it checks if the key already exists -  if so the word is added to the value list. else, a new entry is put
    private void addWordToWordOccurrencesMap(Map<Integer, List<String>> searchWordOccurrences, Map.Entry<Integer, String> entry, String word) {
        List<String> values;
        Integer timeKey = entry.getKey();

        //check if there are other words from desired words list that already included in this key value pair
        if (searchWordOccurrences.containsKey(timeKey)) {
            values = searchWordOccurrences.get(timeKey);
        } else {
            values = new ArrayList<>();
        }

        values.add(word);
        searchWordOccurrences.put(timeKey, values);
    }

    //all words include (the order don't matter) in max 3 adjacent time slots
    private List<Integer> getExpressionOccurrences(List<String> words) {
        List<Integer> expressionOccurrences = new LinkedList<>();
        Map<Integer, List<String>> wordsOccurrencesMap = createWordsOccurrencesMap(words);

        if (!wordsOccurrencesMap.isEmpty()) {
            expressionOccurrences = getWordsOccurrences(words, wordsOccurrencesMap);
        }

        return expressionOccurrences;
    }

    private List<Integer> getWordsOccurrences(List<String> words, Map<Integer, List<String>> wordsOccurrencesMap) {
        List<Integer> sortedTimeSlots = wordsOccurrencesMap.keySet().stream().sorted().collect(Collectors.toList());
        Set<Integer> occurrences = new HashSet<>();

        for (int i = 0; i < sortedTimeSlots.size(); i++) {
            Integer time = sortedTimeSlots.get(i);
            if (wordsOccurrencesMap.get(time).size() > 0) {
                // all words appear in same sentence
                if (wordsOccurrencesMap.get(time).size() == words.size()) {
                    occurrences.add(time);
                } else {
                    handleNotAllWordsAppearInSameSentence(words, wordsOccurrencesMap, sortedTimeSlots, occurrences, i);
                }
            }
        }

        return new LinkedList<>(occurrences);
    }

    private void handleNotAllWordsAppearInSameSentence(List<String> words, Map<Integer, List<String>> wordsOccurrencesMap, List<Integer> sortedTimeSlots, Set<Integer> occurrences, int index) {
        //check if all words appear in maximum 3 adjacent time slots
        Integer time = sortedTimeSlots.get(index);
        List<String> currSentenceList = wordsOccurrencesMap.get(time);
        Set<String> allThreeSentencesWords = new HashSet<>(currSentenceList);

        Integer occurTime = addPervSentence(wordsOccurrencesMap, sortedTimeSlots, index, time, allThreeSentencesWords);
        addNextSentence(wordsOccurrencesMap, sortedTimeSlots, index, time, allThreeSentencesWords);

        if (allThreeSentencesWords.size() == words.size()) {
            if (occurrences == null) {
                occurrences = new HashSet<>();
            }
            occurrences.add(occurTime);
        }
    }

    private Integer addPervSentence(Map<Integer, List<String>> wordsOccurrencesMap, List<Integer> sortedTimeSlots, int i, Integer time, Set<String> allThreeSentencesWords) {
        Integer occurTime = time;
        if (i > 0) {
            Integer prevTime = sortedTimeSlots.get(i - 1);
            if (time - prevTime <= MAX_DIFF) {
                allThreeSentencesWords.addAll(wordsOccurrencesMap.get(prevTime));
                occurTime = prevTime;
            }
        }

        return occurTime;
    }

    private void addNextSentence(Map<Integer, List<String>> wordsOccurrencesMap, List<Integer> sortedTimeSlots, int i, Integer time, Set<String> allThreeSentencesWords) {
        if (i < sortedTimeSlots.size() - 1) {
            Integer nextTime = sortedTimeSlots.get(i + 1);
            if (nextTime - time <= MAX_DIFF)
                allThreeSentencesWords.addAll(wordsOccurrencesMap.get(nextTime));
        }
    }

    private List<Integer> getWordOccurrences(String word) {
        Set<Integer> occurrences = new HashSet<>();
        for (Map.Entry<Integer, String> entry : transcriptV2.entrySet()) {
            if (entry.getValue().contains(word)) {
                occurrences.add(entry.getKey());
            }
        }

        return new LinkedList<>(occurrences);
    }

    public GetTyposResponse getTyposV2(String word, String youtubeUrl) throws IOException {
        LOGGER.info(String.format("getting typos from %s for %s", youtubeUrl, word));
        createTranscript(youtubeUrl);

        if (dictionaryWithTranscript == null) {
            createDictionaryWithTranscriptV2();
        }

        List<String> typos = new ArrayList<>();
        if (!dictionaryWithTranscript.contains(word.toLowerCase())) {
            typos = getTyposFromDictionary(word.toLowerCase());
        }
        LOGGER.info("finish the 'getTyposV2' operation");
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

    private void createDictionaryWithTranscriptV2() throws IOException {
        //using set in order to avoid duplicates
        Set<String> dictionaryAsSet = getOriginalDictionary();
        dictionaryAsSet.addAll(convertYoutubeTranscriptToWords(transcriptV2.values()));
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

    private Set<String> convertYoutubeTranscriptToWords(Collection<String> youtubeTranscriptSentences) {
        Set<String> youtubeTranscriptWords = new HashSet<>();
        for (String sentence : youtubeTranscriptSentences) {
            youtubeTranscriptWords.addAll(Arrays.asList(sentence.split(DELIMETER)));
        }
        return youtubeTranscriptWords;
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


    /*Old APIs*/

    public GetOccurrencesResponse getOccurrences(String word, String youtubeUrl) {
        LOGGER.info(String.format("getting occurrences from %s for %s", youtubeUrl, word));
        if (!youtubeUrl.equals(transcribedYoutubeUrl)) {
            transcript = YOUTUBE_TRANSCRIPT_SERVICE.getYoutubeTranscript(youtubeUrl);
            transcribedYoutubeUrl = youtubeUrl;
        }

        List<Integer> occurrences = getOccurrencesInTranscript(word.toLowerCase());
        return new GetOccurrencesResponse(occurrences);
    }

    private List<Integer> getOccurrencesInTranscript(String word) {
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
        if (!youtubeUrl.equals(transcribedYoutubeUrl) || transcript == null || dictionaryWithTranscript == null) {
            transcript = YOUTUBE_TRANSCRIPT_SERVICE.getYoutubeTranscript(youtubeUrl);
            transcribedYoutubeUrl = youtubeUrl;
            createDictionaryWithTranscript();
        }
        List<String> typos = new ArrayList<>();
        if (!dictionaryWithTranscript.contains(word.toLowerCase())) {
            typos = getTyposFromDictionary(word.toLowerCase());
        }
        return new GetTyposResponse(typos);
    }

    private void createDictionaryWithTranscript() throws IOException {
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
}
