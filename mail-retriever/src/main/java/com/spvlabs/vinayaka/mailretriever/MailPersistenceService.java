package com.spvlabs.vinayaka.mailretriever;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class MailPersistenceService {
    final MongoTemplate mongoTemplate;

    public MailPersistenceService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public void saveMails(List<GmailPlain> mails) {
        try {
            mongoTemplate.insert(mails, GmailPlain.class);
        } catch (Exception e) {
            log.error("Error occurred while saving.", e);
        }
    }
}
