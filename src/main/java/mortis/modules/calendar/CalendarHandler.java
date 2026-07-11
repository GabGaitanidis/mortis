package mortis.modules.calendar;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.Events;

import mortis.utils.Env;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;

public class CalendarHandler {

    private static final String APPLICATION_NAME = "Mortis Calendar";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = Env.get(
        "GOOGLE_TOKENS_PATH",System.getProperty("user.home") + "/.mortis/tokens");
    private static final String CREDENTIALS_FILE_PATH = Env.get(
        "CREDENTIALS_FILE_PATH", "/home/gabz/Desktop/projects/mortis/files/credentials.json");
    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR);
    private final Calendar service;

    public CalendarHandler() throws Exception {
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        this.service = new Calendar.Builder(httpTransport, JSON_FACTORY, getCredentials(httpTransport))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    private Credential getCredentials(HttpTransport httpTransport) throws Exception {
        InputStream in = new FileInputStream(CREDENTIALS_FILE_PATH);
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();

        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    public List<Event> getTodayEvents() throws Exception {
        ZoneId zone = ZoneId.systemDefault();
        LocalDate today = LocalDate.now();
        DateTime startOfDay = new DateTime(today.atStartOfDay(zone).toInstant().toEpochMilli());
        DateTime endOfDay = new DateTime(today.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli());

        Events events = service.events().list("primary")
                .setTimeMin(startOfDay)
                .setTimeMax(endOfDay)
                .setOrderBy("startTime")
                .setSingleEvents(true)
                .execute();

        return events.getItems();
    }

    public Event createEvent(String title, LocalDateTime start, LocalDateTime end) throws IOException {
        ZoneId zone = ZoneId.systemDefault();
        Event event = new Event().setSummary(title);
        DateTime starDateTime = new DateTime(start.atZone(zone).toInstant().toEpochMilli());
        EventDateTime eventStarTime = new EventDateTime().setDateTime(starDateTime).setTimeZone(zone.getId());
        event.setStart(eventStarTime);

        DateTime endDateTime = new DateTime(end.atZone(zone).toInstant().toEpochMilli());
        EventDateTime eventEndTime = new EventDateTime().setDateTime(endDateTime).setTimeZone(zone.getId());
        event.setEnd(eventEndTime);

        return service.events().insert("primary", event).execute();
    }
}