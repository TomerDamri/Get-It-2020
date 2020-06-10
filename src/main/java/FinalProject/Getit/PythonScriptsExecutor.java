package FinalProject.Getit;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class PythonScriptsExecutor {
    Process mProcess;

    public Map<String, String> runScript(String scriptPath, String youtubeUrl, String keyword) {
        Process process;
        HashMap<String, String> allOccurrences = null;
        try {
            process = Runtime.getRuntime().exec(String.format("python %s %s %s", scriptPath, youtubeUrl, keyword));
            mProcess = process;
        } catch (Exception e) {
            System.err.println("Exception Raised" + e.toString());
        }
        InputStream stdout = mProcess.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(stdout, StandardCharsets.UTF_8));
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