package mortis.ai;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import mortis.utils.Env;
import mortis.utils.Similarity;

public class Memory {

    private static final Path file = Path.of(Env.get("MEMORY_FILE", null));
    ObjectMapper mapper;

    public Memory() throws IOException {
        this.mapper = new ObjectMapper();
        if (Files.notExists(file)) {
            Files.createFile(file);
        }
    }

    public List<String> load() throws IOException {
        return Files.readAllLines(file);
    }

    public void add(ActivityRecord data) throws IOException {
        String json = mapper.writeValueAsString(data);
        Files.writeString(file, json + "\n", StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }
    public List<ActivityRecord> loadRecords() throws IOException {
        List<ActivityRecord> records = new ArrayList<>();
        for (String line : Files.readAllLines(file)) {
            if (line.isBlank()) {
                continue;
            }
            try {
                records.add(mapper.readValue(line, ActivityRecord.class));
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
        return records;
    }

    public List<ActivityRecord> getRecentRecords() throws IOException {
        List<ActivityRecord> records = loadRecords();
        int n = Math.max(0, records.size() - 20);
        return new ArrayList<>(records.subList(n, records.size()) );
    }
    public List<ActivityRecord> searchRelevant(String query, int limit) throws IOException {
    List<ActivityRecord> all = loadRecords();

    return all.stream()
        .map(r -> Map.entry(r, Similarity.tokenOverlap(query, r.activityName())))
        .filter(e -> e.getValue() > 0.1) 
        .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
        .limit(limit)
        .map(Map.Entry::getKey)
        .collect(java.util.stream.Collectors.toList());
}
    
}