package mortis.ai;

import java.time.Instant;

public record ActivityRecord(
    String activityName,
    String module,
    String action,
    String target,     
    Instant timestamp
) {}