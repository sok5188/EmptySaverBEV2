package com.example.emptySaver.domain.entity.category;

public enum SportsType {
    FOOTBALL("축구"),
    BASEBALL("야구"),
    BASKETBALL("농구"),
    BALLING("볼링"),
    BILLIARDS("당구"),
    SQUASH("스쿼시"),
    TENNIS("테니스"),
    PINGPONG("탁구");
    private String value;

    SportsType(String value) {
        this.value = value;
    }

    public String getKey() {
        return name();
    }

    public String getValue() {
        return value;
    }
}
