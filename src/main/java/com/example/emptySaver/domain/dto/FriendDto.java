package com.example.emptySaver.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;


public class FriendDto {
    @Data
    @AllArgsConstructor
    public static class res<T>{
        private T data;
    }
    @Data
    @Builder
    public static class FriendInfo{
        private String friendName;
        private String friendEmail;
        private Long friendId;
        private Long friendMemberId;
    }
}
