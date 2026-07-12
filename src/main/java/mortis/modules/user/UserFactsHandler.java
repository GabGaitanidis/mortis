package mortis.modules.user;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mortis.utils.Env;

public class UserFactsHandler {
    private final Map<String, String> facts;
    private final Path path;

    public UserFactsHandler() throws IOException {
        this.facts = new HashMap<>();
        this.path = Path.of(Env.get(
            "USER_FACTS_PATH", "/home/gabz/Desktop/projects/mortis/files/context.txt"));

        if (Files.notExists(path)) {
            Files.createDirectories(path.getParent());
            Files.createFile(path);
        }

        initializeFacts();
    }

    private List<String> readLines() throws IOException {
        return Files.readAllLines(path);
    }

    private void initializeFacts() throws IOException {
        for (String line : readLines()) {
            if (line.isBlank()) {
                continue;
            }
            String[] parts = line.trim().split("\\s+", 2); 
            if (parts.length < 2) {
                System.err.println("Skipping malformed user fact line: " + line);
                continue;
            }
            facts.put(parts[0], parts[1]);
        }
    }

    public void set(String key, String value) throws IOException {
        facts.put(key, value);
        save();
    }

    public String get(String key) {
        return facts.get(key);
    }

    public void forget(String key) throws IOException {
        facts.remove(key);
        save();
    }

    private void save() throws IOException {
        List<String> lines = facts.entrySet().stream()
            .map(e -> e.getKey() + " " + e.getValue())
            .toList();
        Files.write(path, lines);
    }

    public String getKeysAString() throws IOException {
        StringBuilder keys = new StringBuilder();
        for (String key : facts.keySet()) {
            keys.append(key);
        }

        return keys.toString();
    }
}