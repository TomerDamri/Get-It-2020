package GetIt.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class YoutubeTranscriptService {
    private static final String userDirectory = Paths.get("")
            .toAbsolutePath()
            .toString();
    private static final String scriptPath = (userDirectory.contains("\\")) ? userDirectory + "\\scripts\\youtube_api.py" : userDirectory + "/scripts/youtube_api.py";

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

}