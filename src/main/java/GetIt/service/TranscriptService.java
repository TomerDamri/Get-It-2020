package GetIt.service;

import GetIt.exceptions.InternalServerErrorException;
import GetIt.exceptions.TranscriptNotFoundException;
import GetIt.exceptions.UpdateTranscriptException;
import GetIt.model.repositoriesModels.TranscriptEntity;
import GetIt.repositories.TranscriptRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

@Service
public class TranscriptService {

    private final static Logger LOGGER = Logger.getLogger(Logger.class.getName());
    private static final String userDirectory = Paths.get("")
            .toAbsolutePath()
            .toString();
    private static final String scriptPath = (userDirectory.contains("\\")) ? userDirectory + "\\scripts\\youtube_api.py" : userDirectory + "/scripts/youtube_api.py";
    public static final String TRANSCRIPT_DELIMITER = "&&&";

    private Map<String, Long> youtubeUrlToId = new HashMap<>();

    @Autowired
    private TranscriptRepository transcriptRepository;

    public Map<Integer, String> getTranscript(String youtubeUrl) {
        Map<Integer, String> transcript;
        TranscriptEntity transcriptEntity = getTranscriptFromRepository(youtubeUrl);

        if (transcriptEntity != null) {
            transcript = transcriptEntity.getTranscript();
        } else {
            transcript = getTranscriptFromYoutube(youtubeUrl);
            saveTranscriptToRepository(youtubeUrl, transcript);
        }

        return new TreeMap<>(transcript);
    }

    public void updateTranscript(String youtubeUrl, Integer timeSlots, String oldSentence, String fixedSentence) {
        LOGGER.info(String.format("Updating the transcript of %s", youtubeUrl));
        Map<Integer, String> transcript = getTranscript(youtubeUrl);
        updateSentenceInTranscript(transcript, timeSlots, oldSentence, fixedSentence);
        saveTranscriptToRepository(youtubeUrl, transcript);
        LOGGER.info(String.format("Done updating the transcript of %s", youtubeUrl));
    }

    public Map<Integer, String> getTranscriptFromYoutube(String youtubeUrl) {
        String[] arrOfStr;
        String transcriptSentence = "";
        String line;
        HashMap<Integer, String> transcript = null;

        BufferedReader reader = PythonExecuter.runScript(scriptPath, youtubeUrl);
        try {
            while ((line = reader.readLine()) != null) {
                if (transcript == null) {
                    transcript = new HashMap<>();
                }
                arrOfStr = line.split(TRANSCRIPT_DELIMITER, 2);
                transcriptSentence = transcriptSentence.concat(arrOfStr[0]);
                //check if all the data included in "line" or maybe it last to the next line too
                if (arrOfStr.length == 2) {
                    putSentenceInTranscriptMap(arrOfStr[1], transcriptSentence, transcript);
                    transcriptSentence = "";
                }
            }

            if (transcript == null) {
                throw new TranscriptNotFoundException(youtubeUrl);
            }
        } catch (IOException e) {
            String errorMessage = "Failed to read the output of the python transcript script.";
            LOGGER.warning(errorMessage);
            throw new InternalServerErrorException(errorMessage, e);
        }

        return new TreeMap<>(transcript);
    }

    private void putSentenceInTranscriptMap(String s, String transcriptSentence, HashMap<Integer, String> transcript) {
        Float v = Float.parseFloat(s);
        Integer key = v.intValue();

        // check if the key already exist
        if (transcript.containsKey(key)) {
            String prevValue = transcript.get(key);
            transcriptSentence = new StringBuilder(prevValue).append(" ").append(transcriptSentence).toString();
        }

        transcript.put(key, transcriptSentence);
    }

    @Cacheable(value = "transcripts", key = "#youtubeUrl")
    private TranscriptEntity getTranscriptFromRepository(String youtubeUrl) {
        Long id = youtubeUrlToId.get(youtubeUrl);

        return (id != null) ? transcriptRepository.findById(id).orElse(null) : null;
    }

    @Cacheable(value = "transcripts", key = "#youtubeUrl")
    private TranscriptEntity saveTranscriptToRepository(String youtubeUrl, Map<Integer, String> newTranscript) {
        TranscriptEntity transcriptEntity;
        TranscriptEntity transcriptFromRepository = getTranscriptFromRepository(youtubeUrl);

        if (transcriptFromRepository != null) {
            transcriptEntity = transcriptFromRepository;
            transcriptEntity.setTranscript(newTranscript);
            LOGGER.info("update transcriptEntity from repository");
        } else {
            transcriptEntity = new TranscriptEntity(newTranscript);
            LOGGER.info("First entry to transcriptEntity repository");
        }

        TranscriptEntity save = transcriptRepository.save(transcriptEntity);
        youtubeUrlToId.put(youtubeUrl, save.getId());
        LOGGER.info("Transcript saved successfully");
        return save;
    }

    private void updateSentenceInTranscript(Map<Integer, String> transcript, Integer timeSlots, String oldSentence, String fixedSentence) {
        if (!transcript.containsKey(timeSlots) || !transcript.get(timeSlots).equals(oldSentence)) {
            throw new UpdateTranscriptException(String.format("Failed to update the transcript. There is not a sentence like '%s' in the given time slot.", oldSentence));
        }
        transcript.put(timeSlots, fixedSentence);
        LOGGER.info("Transcript updated successfully");
    }
}