package mortis.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;

public class JPython { 
    public static String getInputFromUser() throws IOException, InterruptedException {
        String pythonExecutable = Env.get("MORTIS_PYTHON_EXECUTABLE", Paths.get(System.getProperty("user.home"), "mortis-stt", "venv", "bin", "python3").toString());
        String sttScript = Env.get("MORTIS_STT_SCRIPT", Paths.get(System.getProperty("user.dir"), "scripts", "stt.py").toString());
        ProcessBuilder pb = new ProcessBuilder(
            pythonExecutable,
            sttScript
        );
       
        Process p = pb.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        StringBuilder output = new StringBuilder();
        while((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }
        p.waitFor();
        System.out.println("RAW OUTPUT: [" + output.toString() + "]");
        String result = output.toString().trim();
    
        if (result.equals("") || result.isEmpty()) {
            return null;
        }
        
        return result;
    }


}