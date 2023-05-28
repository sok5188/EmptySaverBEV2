package com.example.emptySaver.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.checkerframework.checker.units.qual.A;

import java.util.List;

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
    @Data
    @AllArgsConstructor
    public static class fullCategoryInfo<T>{
        private String type;
        private String typeName;
        private T result;
    }
    @Data
    @AllArgsConstructor
    public static class memberInterestForm{
        private String type;
        private String typeName;
        private List<String> tagList;

    }
    @Data
    @AllArgsConstructor
    public static class saveMemberInterestReq{
        private String email;
        private List<memberInterestForm> formList;
    }
}
