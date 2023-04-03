package com.example.emptySaver.utils;

import com.example.emptySaver.domain.entity.Subject;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class UosApiBuilder {
    private static final String apiKey = "202303727IDO15243";

    public String buildRequestURL(String url, Map<String, String> params){
        StringBuilder stringBuilder = new StringBuilder(url);
        stringBuilder.append("?apiKey=" + apiKey);

        Set<String> keySet = params.keySet();
        for ( String key : keySet) {
            String value = params.get(key);
            stringBuilder.append("&" + key + "=" + value);
        }

        return stringBuilder.toString();
    }

    public List<Subject> parseSubjectsHtmlData(String subjectHtmlData){
        String[] splitSubject = subjectHtmlData.split("<list>");

        List<Subject> subjects = new ArrayList<>();
        subjects.add(buildSubjectByHtmlData(subjectHtmlData));
        return  subjects;
    }

    private Subject buildSubjectByHtmlData(String subjectHtmlData){
        //data parsing
        String[] splitSubject = subjectHtmlData.split("<|>|/");
        List<String> splitData = new ArrayList<>();

        for (String parsed: splitSubject)
            if(!parsed.isEmpty())
                splitData.add(parsed);

        //data mapping
        Map<String, String> dataMap = new HashMap<>();
        for (int i = 0; i <splitData.size() -1 ; i+=3) {
            String dataName = splitData.get(i);
            String data = splitData.get(i+1);

            if (data.length() >=10) //단순 데이터가 아니면
                data = data.substring(8, data.length()-2);

            dataMap.put(dataName,data);
        }

        Subject subject = Subject.builder().subject_nm(dataMap.get("subject_nm")).credit(Integer.parseInt(dataMap.get("credit"))).build();
        return subject;
    }

}
