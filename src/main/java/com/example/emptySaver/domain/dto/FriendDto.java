package com.example.emptySaver.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;


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

    @Data
    @Builder
    @Schema(description = "친구의 멤버 (friendMemberId)id값을 list로 받음(friendId가 아님에 주의)")
    public static class FriendMemberIdList{
        private List<Long> friendMemberIdLst;
    }
}
