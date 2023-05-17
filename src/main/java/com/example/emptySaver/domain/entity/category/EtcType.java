package com.example.emptySaver.domain.entity.category;

public enum EtcType {
    FREE("자율");
    private String label;
    EtcType(String label){
        this.label=label;
    }

    public String getLabel() {
        return label;
    }
    public String getKey(){
        return name();
    }
}
