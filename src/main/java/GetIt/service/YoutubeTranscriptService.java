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
import java.util.logging.Logger;

public class YoutubeTranscriptService {
    private final static Logger LOGGER = Logger.getLogger(Logger.class.getName());
    private static final String userDirectory = Paths.get("")
            .toAbsolutePath()
            .toString();
    private static final String scriptPath = userDirectory + "/scripts/youtube_api.py";

    public Map<String, String> getYoutubeTranscript(String youtubeUrl) {
        HashMap<String, String> transcript = null;
        BufferedReader reader = PythonExecuter.runScript(scriptPath, youtubeUrl);
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                if (transcript == null) {
                    transcript = new HashMap<>();
                }
                String[] arrOfStr = line.split("&&&", 2);
                if( arrOfStr.length == 2){
                    transcript.put(arrOfStr[0], arrOfStr[1]);
                }
                else{
                    LOGGER.warning("the bad line was:" + line);
                }
            }
        } catch (IOException e) {
            System.err.println("Exception in reading output" + e.toString());
        }

        return transcript;
    }

}