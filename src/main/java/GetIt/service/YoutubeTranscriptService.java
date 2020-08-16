package GetIt.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class YoutubeTranscriptService {
    private static final String userDirectory = Paths.get("")
            .toAbsolutePath()
            .toString();
    private static final String scriptPath = (userDirectory.contains("\\")) ? userDirectory + "\\scripts\\youtube_api.py" : userDirectory + "/scripts/youtube_api.py";
    public static final String TRANSCRIPT_DELIMETER = "&&&";

    public Map<String, String> getYoutubeTranscript(String youtubeUrl) {
        String[] arrOfStr;
        String transcriptSentence = "";
        HashMap<String, String> transcript = null;
        BufferedReader reader = PythonExecuter.runScript(scriptPath, youtubeUrl);
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                if (transcript == null) {
                    transcript = new HashMap<>();
                }
                arrOfStr = line.split("&&&", 2);
                transcriptSentence = transcriptSentence.concat(arrOfStr[0]);
                if (arrOfStr.length == 2) {
                    transcript.put(transcriptSentence, arrOfStr[1]);
                    transcriptSentence = "";
                }
            }
        } catch (IOException e) {
            System.err.println("Exception in reading output" + e.toString());
        }
        return transcript;
    }

    public Map<Integer, String> getYoutubeTranscriptV2(String youtubeUrl) {
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
                arrOfStr = line.split(TRANSCRIPT_DELIMETER, 2);
                transcriptSentence = transcriptSentence.concat(arrOfStr[0]);
                //check if all the data included in "line" or maybe it last to the next line too
                if (arrOfStr.length == 2) {
                    putSentenceInTranscriptMap(arrOfStr[1], transcriptSentence, transcript);
                    transcriptSentence = "";
                }
            }
        } catch (IOException e) {
            System.err.println("Exception in reading output" + e.toString());
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

}