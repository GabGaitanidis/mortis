package mortis.ai;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.ArrayList;




public class ActivityMemory {
    private static final int MAX_SIZE = 15;
    private final Deque<ActivityRecord> activities = new ArrayDeque<>();

    public void add(ActivityRecord record) {
        if (activities.size() >= MAX_SIZE) {
            activities.removeLast(); 
        }
        activities.addFirst(record);
    }

    public List<ActivityRecord> recent() {
        return new ArrayList<>(activities);
    }

    public String toPromptContext() {
        StringBuilder sb = new StringBuilder();
        for (ActivityRecord r : activities) {
            sb.append(String.format("- %s (%s.%s): %s%n", r.activityName(), r.module(), r.action(), r.target()));
        }
        return sb.toString();
    }
}