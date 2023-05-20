package com.example.emptySaver.domain.entity.category;

public enum FreeType {
    FREE("자율");
    private String label;
    FreeType(String label){
        this.label=label;
    }

    public String getLabel() {
        return label;
    }
    public String getKey(){
        return name();
    }
}
