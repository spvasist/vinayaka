package com.spvlabs.vinayaka.mailretriever;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
@EnableScheduling
@SpringBootApplication
public class MailRetrieverApplication {

    private static final String APPLICATION_NAME = "Gmail API Java Quickstart";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Arrays.asList(GmailScopes.GMAIL_COMPOSE
            , GmailScopes.MAIL_GOOGLE_COM
            , GmailScopes.GMAIL_COMPOSE
            , GmailScopes.GMAIL_INSERT
            , GmailScopes.GMAIL_MODIFY
            , GmailScopes.GMAIL_READONLY
            , GmailScopes.GMAIL_LABELS
    );
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    private final ConfigurableApplicationContext ctx;
    private final MailPersistenceService mailPersistenceService;
    AtomicBoolean shutdown = new AtomicBoolean(false);

    public MailRetrieverApplication(MailPersistenceService mailPersistenceService, ConfigurableApplicationContext ctx) throws GeneralSecurityException, IOException, InterruptedException {
        this.ctx = ctx;
        this.mailPersistenceService = mailPersistenceService;
    }

    @PostConstruct
    void run() throws GeneralSecurityException, IOException {
        while (true) {
            System.out.print("***Enter the date range (e.g. 01/01/2020-31/01/2020) & any invalid input to exit *** : ");
            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine();
            String[] split = input.split("-");
            if (split == null || split.length != 2) {
                log.error("Invalid input. Exiting application...");
                break;
            } else {
                // Build a new authorized API client service.
                final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
                Gmail service = new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                        .setApplicationName(APPLICATION_NAME)
                        .build();

                String[] fromParts = split[0].split("/");
                String from = fromParts[2] + "/" + fromParts[1] + "/" + fromParts[0];

                String[] toParts = split[1].split("/");
                String to = toParts[2] + "/" + toParts[1] + "/" + toParts[0];

                // Print the labels in the user's account.
                String user = "me";

                String nextPageToken = null;
                int page = 0;
                int count = 0;
                List<GmailPlain> messageList = new ArrayList<>();
                do {
                    String q = String.format("after:%s before:%s", from, to);
                    ListMessagesResponse msgListResponse = service.users().messages().list(user).setQ(q).setPageToken(nextPageToken).execute();
                    nextPageToken = msgListResponse.getNextPageToken();
                    List<Message> messages = msgListResponse.getMessages();
                    if (messages != null) {
                        //Message message = service.users().messages().get(user, messages.get(0).getId()).execute();
                        messages.stream().parallel().forEach(message -> {
                            try {
                                messageList.add(GmailPlain.getMessage(service, user, message.getId()));
                            } catch (IOException e) {
                                //
                            }
                        });
                        mailPersistenceService.saveMails(messageList);
                    }
                    count += messageList.size();
                    System.out.println(count + " mails saved. Next page token = " + nextPageToken);
                    messageList.clear();
                    page++;
                } while (nextPageToken != null);
            }
        }
        shutdown.set(true);
    }

    /**
     * Creates an authorized Credential object.
     *
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = MailRetrieverApplication.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    public static void main(String[] args) {
        SpringApplication.run(MailRetrieverApplication.class, args);
    }

    @Scheduled(fixedDelay = 1000)
    void shutdown() {
        if(shutdown.get()) {
            try {
                ctx.stop();
                ctx.close();
            } catch (Exception e) {
//
            }
        }
    }
}
