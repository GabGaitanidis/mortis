package mortis.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public final class Env {
    private static final Map<String, String> dotenv = loadDotenv();

    private Env() {
    }

    public static String get(String key, String defaultValue) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            value = dotenv.get(key);
        }
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value;
    }

    private static Map<String, String> loadDotenv() {
        Map<String, String> map = new HashMap<>();
        Path envPath = Path.of(".env");
        if (!Files.exists(envPath)) {
            return map;
        }
        try {
            for (String line : Files.readAllLines(envPath)) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                int idx = line.indexOf('=');
                if (idx == -1) continue;
                String key = line.substring(0, idx).trim();
                String value = line.substring(idx + 1).trim();
                if (value.length() >= 2 &&
                    ((value.startsWith("\"") && value.endsWith("\"")) ||
                     (value.startsWith("'") && value.endsWith("'")))) {
                    value = value.substring(1, value.length() - 1);
                }
                map.put(key, value);
            }
        } catch (IOException e) {
            System.err.println("Failed to load .env: " + e.getMessage());
        }
        return map;
    }
}