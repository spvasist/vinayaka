package com.spvlabs.mailcategorizer.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "word_overlap")
public class WordOverlap {
    private String firstMailId;
    private String secondMailId;
    private Overlap firstMail;
    private Overlap secondMail;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Overlap{
        private double allMatch;
        private double oneCharMatch;
        private double twoCharMatch;
        private double threeCharMatch;
        private double fourCharMatch;
        private double aboveFourCharMatch;
    }
}
