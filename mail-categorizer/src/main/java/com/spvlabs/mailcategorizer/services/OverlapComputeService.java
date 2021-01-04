package com.spvlabs.mailcategorizer.services;

import com.spvlabs.mailcategorizer.entities.GmailPlain;
import com.spvlabs.mailcategorizer.entities.WordOverlap;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class OverlapComputeService {

    final MongoTemplate mongoTemplate;

    public OverlapComputeService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public void computeAndSaveOverlap() {
        List<GmailPlain> mails = mongoTemplate.findAll(GmailPlain.class);
        List<WordOverlap> overlapList = new ArrayList<>();
        for (int i = 0; i < mails.size(); i++) {
            for (int j = i + 1; j < mails.size(); j++) {
                overlapList.add(computeOverlap(mails.get(i), mails.get(j)));
                if (overlapList.size() == 500) {
                    mongoTemplate.insertAll(overlapList);
                    overlapList.clear();
                }
            }
        }
        mongoTemplate.insertAll(overlapList);
        overlapList.clear();
    }

    private WordOverlap computeOverlap(GmailPlain firstMail, GmailPlain secondMail) {
        WordOverlap.Overlap first = computeOverlapMetrics(firstMail, secondMail);
        WordOverlap.Overlap second = computeOverlapMetrics(secondMail, firstMail);
        WordOverlap overlap = new WordOverlap();
        overlap.setFirstMailId(firstMail.getId());
        overlap.setSecondMailId(secondMail.getId());
        overlap.setFirstMail(first);
        overlap.setSecondMail(second);
        return overlap;
    }

    private WordOverlap.Overlap computeOverlapMetrics(GmailPlain firstMail, GmailPlain secondMail) {
        double all = computeOverlapMetrics(firstMail.getPlainBody(), secondMail.getPlainBody(), 0, 1000);
        double one = computeOverlapMetrics(firstMail.getPlainBody(), secondMail.getPlainBody(), 1, 1);
        double two = computeOverlapMetrics(firstMail.getPlainBody(), secondMail.getPlainBody(), 2, 2);
        double three = computeOverlapMetrics(firstMail.getPlainBody(), secondMail.getPlainBody(), 3, 3);
        double four = computeOverlapMetrics(firstMail.getPlainBody(), secondMail.getPlainBody(), 4, 4);
        double aboveFour = computeOverlapMetrics(firstMail.getPlainBody(), secondMail.getPlainBody(), 5, 1000);
        return new WordOverlap.Overlap(all, one, two, three, four, aboveFour);
    }

    private double computeOverlapMetrics(String first, String second, int wlMin, int wlMax) {

        String[] firstWords = first
                .split("~|`|!|@|#|[$]|%|[\\^]|&|[*]|[(]|[)]|_|-|[+]|=|[\\[]|[]\\]]|[\\{]|[\\}]|[\\|]|:|;|\"|'|<|,|>|\\.|\\?|/|[ ]");
        String[] secondWords = second
                .split("~|`|!|@|#|[$]|%|[\\^]|&|[*]|[(]|[)]|_|-|[+]|=|[\\[]|[]\\]]|[\\{]|[\\}]|[\\|]|:|;|\"|'|<|,|>|\\.|\\?|/|[ ]");
        Set<String> firstList = Arrays.stream(firstWords)
                .filter(s -> s != null && s.length() >= wlMin && s.length() <= wlMax)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
        Set<String> secondList = Arrays.stream(secondWords)
                .filter(s -> s != null && s.length() >= wlMin && s.length() <= wlMax)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
        if (firstList.size() == 0 || secondList.size() == 0) {
            return 0;
        }
        Set<String> intersect = new HashSet<>(firstList);
        intersect.retainAll(secondList);
        if (intersect.size() == 0) {
            return 0;
        }
        return (double) intersect.size() / (double) firstList.size();
    }

}
