package GetIt.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class YoutubeTranscriptService {
    private static final String userDirectory = Paths.get("")
            .toAbsolutePath()
            .toString();
    private static final String scriptPath = userDirectory + "/scripts/youtube_api.py";

    public Map<String, String> getYoutubeTranscript(String youtubeUrl) {
        HashMap<String, String> allOccurrences = null;
        BufferedReader reader = PythonExecuter.runScript(scriptPath, youtubeUrl);
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                if (allOccurrences == null) {
                    allOccurrences = new HashMap<>();
                }
                String[] arrOfStr = line.split("&&&", 2);
                allOccurrences.put(arrOfStr[0], arrOfStr[1]);
            }
        } catch (IOException e) {
            System.err.println("Exception in reading output" + e.toString());
        }

        return allOccurrences;
    }

}