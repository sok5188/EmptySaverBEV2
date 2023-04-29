package com.example.emptySaver.domain.entity.category;

public enum StudyType {
    LANGUAGE("어학"),
    JOB("취업"),
    HOBBY("취미"),
    PROGRAMMING("개발");


    private String value;

    StudyType(String value) {
        this.value = value;
    }

    public String getKey() {
        return name();
    }

    public String getValue() {
        return value;
    }
}
