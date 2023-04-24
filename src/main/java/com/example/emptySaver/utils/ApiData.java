package com.example.emptySaver.utils;

public enum ApiData {
    KEY("202303727IDO15243"),
    SUBJECT_URL("https://wise.uos.ac.kr/uosdoc/api.ApiUcrMjTimeInq.oapi"),
    GET("GET"),
    ERROR("error");

    private final String data;

    ApiData(String data){
        this.data = data;
    }

    public String getData() {
        return data;
    }
}
