package mortis.ai;


public record ActivityRecord(
    String activityName,
    String module,
    String action,
    String target,     
    String timestamp
) {}