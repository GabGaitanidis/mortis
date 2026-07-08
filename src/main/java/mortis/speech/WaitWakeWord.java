package mortis.speech;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Path;

import mortis.utils.Env;

public class WaitWakeWord implements Speech {
    private Process process;
    private BufferedWriter writer;
    private BufferedReader reader;

    public void start() throws IOException {
        String executable = Env.get("MORTIS_PYTHON_EXECUTABLE", "python3");
        String script = Env.get("MORTIS_WAKE_SCRIPT",
                Path.of(System.getProperty("user.dir"), "scripts", "wake.py").toString());

        ProcessBuilder pb = new ProcessBuilder(executable, "-u", script);
        pb.redirectErrorStream(false);
        this.process = pb.start();
        this.writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
        this.reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        String ready = reader.readLine();
        if (ready == null || !ready.equals("READY")) {
            throw new IOException("Wake word process did not start correctly, got: " + ready);
        }
    }

    public void waitForWakeWord() throws IOException {
        writer.write("listen\n");
        writer.flush();

        String line = reader.readLine();
        if (line == null) {
            throw new IOException("Wake word process closed the pipe unexpectedly");
        }
        if (!line.equals("detected")) {
            throw new IOException("Unexpected wake word response: " + line);
        }
    }

    @Override
    public void shutdown() throws IOException {
            if (writer != null) {
                writer.write("__EXIT__");
                writer.newLine();
                writer.flush();
                writer.close();
            }
            if (reader != null) reader.close();
            if (process != null) {
                try {
                    boolean exited = process.waitFor(2, java.util.concurrent.TimeUnit.SECONDS);
                    if (!exited) {
                        process.destroyForcibly();
                    }
                } catch (InterruptedException e) {
                    process.destroyForcibly();
                }
            }
    
    }
}