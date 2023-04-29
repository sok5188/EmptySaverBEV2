package com.example.emptySaver.domain.entity.category;

public enum GameType {
    FPS("FPS"),RTS("RTS"),AOS("AOS"),MMORPG("MMORPG");
    private String value;

    GameType(String value) {
        this.value = value;
    }

    public String getKey() {
        return name();
    }

    public String getValue() {
        return value;
    }
}
