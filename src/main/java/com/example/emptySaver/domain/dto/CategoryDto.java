package com.example.emptySaver.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

public class CategoryDto {
    @Data
    @AllArgsConstructor
    public static class res<T>{
        private T result;
    }
    @Data
    @AllArgsConstructor
    public static class categoryType<T>{
        private String type;
        private T result;
    }
    @Data
    @AllArgsConstructor
    public static class categoryInfo{
        private String type;
        private String name;
    }
    @Data
    @AllArgsConstructor
    public static class labelInfo{
        private String key;
        private String name;
    }
}
