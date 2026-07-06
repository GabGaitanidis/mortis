package mortis.speech;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Path;

import mortis.utils.Env;

public class TtsBridge {
    private Process process;
    private BufferedWriter writer;
    private BufferedReader reader;

    public void start() throws IOException {
        String pythonExecutable = Env.get("MORTIS_PYTHON_EXECUTABLE", "python3");
        String script = Env.get("MORTIS_TTS_SCRIPT", Path.of(System.getProperty("user.dir"), "scripts", "tts.py").toString());
        
        ProcessBuilder pb = new ProcessBuilder(pythonExecutable, "-u", script);
        pb.redirectErrorStream(false);
        this.process = pb.start();
        this.writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
        this.reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        Thread errDrain = new Thread(() -> {
        try (BufferedReader err = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = err.readLine()) != null) {
                    System.err.println("[tts-stderr] " + line);
                }
            } catch (IOException ignored) {}
        });
        errDrain.setDaemon(true);
        errDrain.start();
    }

    public void speak(String text) throws IOException {
        String sanitized = text.replace("\n", " ").replace("\r", " ");
        writer.write(sanitized);
        writer.newLine();
        writer.flush();
    }

    public void awaitDone() throws IOException {
        String line = reader.readLine();
        if (line == null) {
            throw new IOException("TTS process closed pipe before signaling done");
        }
        if (!"DONE".equals(line.trim())) {
            System.err.println("Unexpected TTS signal line: " + line);
        }
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

}
