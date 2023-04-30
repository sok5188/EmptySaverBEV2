package com.example.emptySaver.domain.entity.category;

public enum MovieType {
    SF("공상과학"),
    FANTASY("판타지"),
    ROMANCE("로맨스"),
    ACTION("액션");

    private String value;

    MovieType(String value) {
        this.value = value;
    }

    public String getKey() {
        return name();
    }

    public String getValue() {
        return value;
    }
}
