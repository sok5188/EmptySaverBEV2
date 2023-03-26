package com.example.emptySaver.utils;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

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
}
