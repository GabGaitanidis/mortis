package mortis.modules.calendar;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import com.google.api.services.calendar.model.Event;

import mortis.core.Command;
import mortis.core.Module;
import mortis.speech.TtsBridge;

public class CalendarModule implements Module {

    private CalendarHandler handler;

    public CalendarModule() {
        try {
            this.handler = new CalendarHandler();
        } catch (Exception e) {
            System.err.println("Failed to initialize CalendarHandler: " + e.getMessage());
        }
    }

    @Override
    public void execute(Command command, TtsBridge ttsBridge) throws IOException, URISyntaxException {
        if (handler == null) {
            speak("Calendar isn't available right now.", ttsBridge);
            return;
        }

        switch (command.getAction()) {
            case "get_today_events":
                try {
                    List<Event> events = handler.getTodayEvents();
                    speak(formatEvents(events), ttsBridge);
                } catch (Exception e) {
                    speak("I couldn't reach your calendar.", ttsBridge);
                }
                break;

            default:
                speak("I don't know how to do that with your calendar.", ttsBridge);
                break;
        }
    }

    private String formatEvents(List<Event> events) {
        if (events == null || events.isEmpty()) {
            return "You have no events today.";
        }

        StringBuilder response = new StringBuilder("Here's what you have today: ");
        for (Event event : events) {
            String time = event.getStart().getDateTime() != null
                    ? event.getStart().getDateTime().toString()
                    : "all day";
            response.append(event.getSummary()).append(" at ").append(time).append(". ");
        }
        return response.toString();
    }

   
}