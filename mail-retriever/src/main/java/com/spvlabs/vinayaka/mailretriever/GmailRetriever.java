package com.spvlabs.vinayaka.mailretriever;

import java.io.IOException;
//import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartHeader;

//import javax.mail.MessagingException;
//import javax.mail.Session;
//import javax.mail.internet.MimeMessage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class GmailRetriever {

    // ...


    /**
     * Get Message with given ID.
     *
     * @param service   Authorized Gmail API instance.
     * @param userId    User's email address. The special value "me"
     *                  can be used to indicate the authenticated user.
     * @param messageId ID of Message to retrieve.
     * @return Message Retrieved Message.
     * @throws IOException
     */
    public static GmailPlain getMessage(Gmail service, String userId, String messageId)
            throws IOException {
        Message message = service.users().messages().get(userId, messageId).execute();

        System.out.println("Message snippet: " + message.getSnippet());
        return getMail(message);
    }

//    /**
//     * Get a Message and use it to create a MimeMessage.
//     *
//     * @param service   Authorized Gmail API instance.
//     * @param userId    User's email address. The special value "me"
//     *                  can be used to indicate the authenticated user.
//     * @param messageId ID of Message to retrieve.
//     * @return MimeMessage MimeMessage populated from retrieved Message.
//     * @throws IOException
//     * @throws MessagingException
//     */
//    public static MimeMessage getMimeMessage(Gmail service, String userId, String messageId)
//            throws IOException, MessagingException {
//        Message message = service.users().messages().get(userId, messageId).setFormat("raw").execute();
//
//        Base64 base64Url = new Base64(true);
//        byte[] emailBytes = base64Url.decodeBase64(message.getRaw());
//
//        Properties props = new Properties();
//        Session session = Session.getDefaultInstance(props, null);
//
//        MimeMessage email = new MimeMessage(session, new ByteArrayInputStream(emailBytes));
//
//        return email;
//    }

    static GmailPlain getMail(Message message) {
        GmailPlain gmail = new GmailPlain();
        gmail.setHeaders(
                message.getPayload().getHeaders().stream().collect(
                        Collectors.toMap(MessagePartHeader::getName
                                , MessagePartHeader::getValue
                                , (ov, nv) -> ov)));
        getBody(message.getPayload(), gmail.getBodyList());
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
