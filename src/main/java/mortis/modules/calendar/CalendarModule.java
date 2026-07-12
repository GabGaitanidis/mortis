package mortis.modules.calendar;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
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
            case "create_event":
                try {
                    Event event = handler.createEvent(command.get("summary").toString(), parseDateTime(command.get("start")), parseDateTime(command.get("end")));
                    speak("I will add this to your calendar with the summary: "  + event.getSummary(), ttsBridge);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    speak("I cant add this to your calendar", ttsBridge);
                }
          
        }
    }

    private String formatEvents(List<Event> events) {
        if (events == null || events.isEmpty()) {
            return "You have no events today.";
        }

        StringBuilder response = new StringBuilder("Here's what you have today: ");
        for (Event event : events) {
            String time = formatEventTime(event);
            response.append(event.getSummary()).append(" at ").append(time).append(". ");
        }
        return response.toString();
    }

    private String formatEventTime(Event event) {
        if (event.getStart().getDateTime() == null) {
            return "all day";
        }

        long millis = event.getStart().getDateTime().getValue();
        java.time.Instant instant = java.time.Instant.ofEpochMilli(millis);
        java.time.ZonedDateTime zoned = instant.atZone(java.time.ZoneId.systemDefault());

        return java.time.format.DateTimeFormatter.ofPattern("h:mm a").format(zoned);
    }

   private LocalDateTime parseDateTime(Object value) {
        if (value == null) {
            throw new IllegalArgumentException("Missing datetime value");
        }
        String str = value.toString().trim();
        try {
            return LocalDateTime.parse(str);
        } catch (java.time.format.DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid datetime format: " + str, e);
        }
    }
}