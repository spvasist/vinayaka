package com.spvlabs.vinayaka.mailretriever;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartHeader;
import lombok.Getter;
import org.jsoup.Jsoup;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Document(collection = "mail")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GmailPlain {
    @Getter
    private String from;
    @Getter
    private String to;
    @Getter
    private String subject;
    @Getter
    private String date;
    @Getter
    private String messageId;
    @Getter
    private String plainBody;
    @Getter
    private Map<String, String> headers;
    private final List<String> bodyList = new ArrayList<>();

    private static void updateMainFields(GmailPlain gmail) {
        gmail.from = gmail.headers.get("From");
        gmail.to = gmail.headers.get("To");
        gmail.date = gmail.headers.get("Date");
        gmail.subject = gmail.headers.get("Subject");
        gmail.plainBody = toPlainBody(gmail.bodyList);
    }

    private static String toPlainBody(List<String> bodyList) {
        List<String> list = bodyList.stream().map(b -> Jsoup.parse(b).text()).collect(Collectors.toList());
        return String.join("\n", list);
    }

    public static GmailPlain getMessage(Gmail service, String userId, String messageId)
            throws IOException {
        return getMail(service.users().messages().get(userId, messageId).execute(), messageId);
    }

    public static GmailPlain getMail(Message message, String messageId) {
        GmailPlain gmail = new GmailPlain();
        gmail.messageId = messageId;
        gmail.headers = message.getPayload().getHeaders().stream().collect(
                Collectors.toMap(MessagePartHeader::getName
                        , MessagePartHeader::getValue
                        , (ov, nv) -> ov));
        getBody(message.getPayload(), gmail.bodyList);
        updateMainFields(gmail);
        return gmail;
    }

    static void getBody(MessagePart messagePart, List<String> bodyList) {
        String mimeType = messagePart.getMimeType();
        switch (mimeType) {
            case "text/html":
                bodyList.add(new String(messagePart.getBody().decodeData()));
                break;
            case "multipart/alternative":
            case "multipart/mixed":
                for (MessagePart part : messagePart.getParts()) {
                    getBody(part, bodyList);
                }
        }
    }
}

