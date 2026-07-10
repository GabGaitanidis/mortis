package mortis.modules.memory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.fasterxml.jackson.databind.ObjectMapper;

import mortis.ai.ActivityRecord;
import mortis.utils.Env;
import mortis.utils.Similarity;

public class MemoryHandler {
    private final Path path = Path.of(Env.get("MEMORY_FILE", null));
    private final ObjectMapper mapper = new ObjectMapper();
    public ActivityRecord searchActivity(String activityName) throws IOException {
        Double bestScore = 0.0;
        ActivityRecord bestMatchedRecord = null;
        
        for (String line : Files.readAllLines(path)) {
            if (line.isBlank()) {
                continue;
            }
            try {
               ActivityRecord record = mapper.readValue(line, ActivityRecord.class);
                if (record.activityName().equals(activityName)) {
                    return record; 
                }
                double score = Similarity.tokenOverlap(activityName, record.activityName());
                if (score > bestScore) {
                    bestScore = score;
                    bestMatchedRecord = record;
                }
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
        return bestScore > 0.3 ? bestMatchedRecord : null;
    }
}
