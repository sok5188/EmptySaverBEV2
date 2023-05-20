package com.example.emptySaver.domain.entity.category;

public enum MovieType {
    SF("공상과학"),
    FANTASY("판타지"),
    ROMANCE("로맨스"),
    ADVENTURE("모험"),
    ANIMATION("애니메이션"),
    COMEDY("코미디"),
    CRIME("범죄"),
    DOCUMENTARY("다큐멘터리"),
    DRAMA("드라마"),
    FAMILY("가족"),
    HORROR("공포"),
    MUSICAL("뮤지컬"),
    MYSTERY("미스터리"),
    SPORTS("스포츠"),
    THRILL("스릴러"),
    WAR("전쟁"),
    WESTERN("서부극"),
    ETC("기타"),
    ACTION("액션");

    private String label;

    MovieType(String label) {
        this.label = label;
    }

    public String getKey() {
        return name();
    }

    public String getLabel() {
        return label;
    }
}
