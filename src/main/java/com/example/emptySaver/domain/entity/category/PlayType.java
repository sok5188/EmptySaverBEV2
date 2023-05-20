package com.example.emptySaver.domain.entity.category;

public enum PlayType {
    ONLINE_GAME("온라인게임"), CONSOLE_GAME("콘솔게임"), SING("노래"), BALLING("볼링"),
    BILLIARDS("당구"), DRINK("술"), CHAT("잡담"), ROOM_ESCAPE("방탈출"), BOARD_GAME("보드게임"),
    CARTOON("만화"), ETC("기타");
    private String label;

    PlayType(String label) {
        this.label = label;
    }

    public String getKey() {
        return name();
    }

    public String getLabel() {
        return label;
    }
}
