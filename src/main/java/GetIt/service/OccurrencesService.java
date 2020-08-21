package GetIt.service;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class OccurrencesService {

    public static final Integer MAX_DIFF = 5;
    public static final Integer MIN_DIFF = 3;
    public static final String DELIMETER = " ";

    public List<Integer> getOccurrencesInTranscriptV2(Map<Integer, String> transcript, String word) {
        List<Integer> allOccurrences;
        List<String> words = Arrays.asList(word.split(DELIMETER));

        if (words.size() > 1) {
            allOccurrences = getExpressionOccurrences(transcript, words);
        } else {
            allOccurrences = getWordOccurrences(transcript, word);
        }

        return filterCloseTimeSlotsFromAllOccurrences(allOccurrences);
    }

    private List<Integer> getWordOccurrences(Map<Integer, String> transcript, String word) {
        Set<Integer> occurrences = new HashSet<>();
        for (Map.Entry<Integer, String> entry : transcript.entrySet()) {
            if (entry.getValue().contains(word)) {
                occurrences.add(entry.getKey());
            }
        }

        return new LinkedList<>(occurrences);
    }

    //all words include (the order don't matter) in max 3 adjacent time slots
    private List<Integer> getExpressionOccurrences(Map<Integer, String> transcript, List<String> words) {
        List<Integer> expressionOccurrences = new LinkedList<>();
        Map<Integer, List<String>> wordsOccurrencesMap = createWordsOccurrencesMap(transcript, words);

        if (!wordsOccurrencesMap.isEmpty()) {
            expressionOccurrences = getWordsOccurrences(words, wordsOccurrencesMap);
        }

        return expressionOccurrences;
    }

    //create hashMap of : key = time slot, value = list of words form desired words list that are included in the "key" time slot
    private Map<Integer, List<String>> createWordsOccurrencesMap(Map<Integer, String> transcript, List<String> words) {
        Map<Integer, List<String>> searchWordOccurrences = new HashMap<>();

        for (Map.Entry<Integer, String> entry : transcript.entrySet()) {
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

    private List<Integer> filterCloseTimeSlotsFromAllOccurrences(List<Integer> allOccurrences) {
        List<Integer> filteredList = new LinkedList<>();
        if (allOccurrences.size() > 0) {
            Collections.sort(allOccurrences);
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
        }

        return filteredList;
    }
}
