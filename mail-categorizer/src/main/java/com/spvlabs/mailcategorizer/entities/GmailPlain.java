package com.spvlabs.mailcategorizer.entities;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Data
@Document(collection = "mail")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GmailPlain {
    @Id
    private String id;
    private String from;
    private String to;
    private String subject;
    private String date;
    private String messageId;
    private String plainBody;
    private Map<String, String> headers;
    private List<String> bodyList = new ArrayList<>();
}

