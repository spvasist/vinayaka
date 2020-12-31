package com.spvlabs.vinayaka.mailretriever;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.jsoup.Jsoup;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class GmailPlain {
    private Map<String, String> headers;
    private List<String> bodyList = new ArrayList<>();

    public String toJson(){
        try {
            GmailPlain gp = new GmailPlain();
            gp.setHeaders(this.headers);
            if(bodyList != null){
                List<String> list = bodyList.stream().map(b -> Jsoup.parse(b).text()).collect(Collectors.toList());
                gp.setBodyList(list);
            }
            ObjectMapper m = new ObjectMapper();
            return m.writeValueAsString(gp);
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
        return "";
    }
}

