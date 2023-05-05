package com.example.emptySaver.domain.entity.category;

public enum StudyType {
    LANGUAGE("어학"),
    JOB("취업"),
    HOBBY("취미"),
    PROGRAMMING("개발");


    private String label;

    StudyType(String label) {
        this.label = label;
    }

    public String getKey() {
        return name();
    }

    public String getLabel() {
        return label;
    }
}
