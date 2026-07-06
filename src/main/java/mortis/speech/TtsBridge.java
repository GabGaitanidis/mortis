package mortis.speech;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Path;

public class TtsBridge {
    private Process process;
    private BufferedWriter writer;
    private BufferedReader reader;

    public void start() throws IOException {
        String pythonExecutable = getEnv("MORTIS_PYTHON_EXECUTABLE", "python3");
        String script = getEnv("MORTIS_TTS_SCRIPT", Path.of(System.getProperty("user.dir"), "scripts", "tts.py").toString());
        
        ProcessBuilder pb = new ProcessBuilder(pythonExecutable, "-u", script);
        pb.redirectErrorStream(true);
        this.process = pb.start();
        this.writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
        this.reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
    }

    public void speak(String text) throws IOException {
        String sanitized = text.replace("\n", " ").replace("\r", " ");
        writer.write(sanitized);
        writer.newLine();
        writer.flush();
    }

    public void awaitDone() throws IOException {
        reader.readLine();
    }

    public void shutdown() throws IOException {
        if (writer != null) {
            writer.write("__EXIT__");
            writer.newLine();
            writer.flush();
            writer.close();
        }
        if (reader != null) {
            reader.close();
        }
        if (process != null) {
            process.destroy();
        }
    }

    public void speakOnce(String text) throws IOException {
        try {
            start();
            speak(text);
            awaitDone();
        } finally {
            shutdown();
        }
    }

    private String getEnv(String key, String defaultValue) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value;
    }
}
