package GetIt.service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class PythonExecuter {


    public static BufferedReader runScript(String scriptPath, String youtubeUrl) {
        Process process = null, mProcess;
        try {
            process = Runtime.getRuntime().exec(String.format("python %s %s", scriptPath, youtubeUrl));
        } catch (Exception e) {
            System.err.println("Exception Raised" + e.toString());
        }
        mProcess = process;
        InputStream stdout = mProcess.getInputStream();
        return new BufferedReader(new InputStreamReader(stdout, StandardCharsets.UTF_8));
    }
}
