package com.example.emptySaver.domain.dto;

import com.example.emptySaver.domain.entity.category.GameType;
import lombok.AllArgsConstructor;
import lombok.Builder;
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
}
