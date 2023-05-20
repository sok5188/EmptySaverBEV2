package com.example.emptySaver.domain.entity.category;

public enum SportsType {
    FOOTBALL("축구"),
    BASEBALL("야구"),
    BASKETBALL("농구"),
    BALLING("볼링"),
    BILLIARDS("당구"),
    SQUASH("스쿼시"),
    TENNIS("테니스"),
    PINGPONG("탁구"),
    ETC("기타")
    ;
    private String label;

    SportsType(String label) {
        this.label = label;
    }

    public String getKey() {
        return name();
    }

    public String getLabel() {
        return label;
    }
}
