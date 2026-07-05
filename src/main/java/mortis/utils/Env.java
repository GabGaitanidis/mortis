package mortis.utils;

public final class Env {
    private Env() {
    }

    public static String get(String key, String defaultValue) {
        String value = System.getenv(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value;
    }
}
