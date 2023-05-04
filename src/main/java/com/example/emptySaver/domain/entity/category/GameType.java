package com.example.emptySaver.domain.entity.category;

public enum GameType {
    FPS("FPS"),RTS("RTS"),AOS("AOS"),MMORPG("MMORPG");
    private String label;

    GameType(String label) {
        this.label = label;
    }

    public String getKey() {
        return name();
    }

    public String getLabel() {
        return label;
    }
}
