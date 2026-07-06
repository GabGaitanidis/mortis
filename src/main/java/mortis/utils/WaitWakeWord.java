package mortis.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class WaitWakeWord {
    private final String executable;
    private final String script;

    public WaitWakeWord() {
        this.executable = Env.get("MORTIS_PYTHON_EXECUTABLE", "/home/gabz/mortis-stt/venv/bin/python3");
        this.script = Env.get("MORTIS_MAIN_PY", "/home/gabz/Desktop/projects/mortis/scripts/main.pyw");
    }

    public void waitForWakeWord() throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(executable, "-u", script);
        pb.redirectErrorStream(false);
        Process p = pb.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            String line;
            boolean detected = false;
            while ((line = reader.readLine()) != null) {
                if (line.equals("detected")) {
                    detected = true;
                    break;
                }
            }
            if (!detected) {
                throw new IOException("Wake word process exited without detecting wake word");
            }
        } finally {
            p.waitFor();
        }
    }
}