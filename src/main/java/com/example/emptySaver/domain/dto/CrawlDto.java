package com.example.emptySaver.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

public class CrawlDto {
    @Data
    @AllArgsConstructor
    public static class res<T>{
        private T data;
        private Boolean isEmpty;
    }

    @Data
    @Builder
    public static class crawlData{
        private String courseName;
        private String applyDate;
        private String runDate;
        private String targetDepartment;
        private String targetGrade;
        private String url;
    }

}
