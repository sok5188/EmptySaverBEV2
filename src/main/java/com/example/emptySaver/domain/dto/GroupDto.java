package com.example.emptySaver.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

public class GroupDto {


    @Data
    @AllArgsConstructor
    public static class res<T>{
        private T data;
    }
    @Data
    @AllArgsConstructor
    public static class GroupMemberRes<T>{
        private T data;
        private Boolean isAnonymous;
    }
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GroupInfo{
        private String groupName;
        private String oneLineInfo;
        private String groupDescription;

        private Long maxMember;

        private Boolean isPublic;
        private Boolean isAnonymous;
        private String categoryName;
        private String labelName;

    }
    @Data
    @Builder
    public static class SimpleGroupRes{
        private Long groupId;
        private String groupName;
        private String oneLineInfo;

        private Long nowMember;
        private Long maxMember;

        private Boolean isPublic;
        private Boolean isAnonymous;

        private String categoryLabel;
        private Boolean amIOwner;
        private String categoryName;
    }
    @Data
    @Builder
    public static class memberGroupReq{
        private Long memberId;
        private Long groupId;
    }
    @Data
    @Builder
    public static class DetailGroupRes{
        private Long groupId;
        private String groupName;
        private String oneLineInfo;
        private String groupDescription;
        private Long nowMember;
        private Long maxMember;

        private Boolean isPublic;
        private Boolean isAnonymous;

        private String categoryLabel;

        private List<CommentDto.CommentRes> commentList;
        private Boolean amIOwner;
        private String categoryName;
    }

    @Data
    @Builder
    public static class InviteInfo{
        private Long memberTeamId;
        private Long memberId;
        private Long groupId;
        private String groupName;
        private String memberName;
        private LocalDateTime inviteDate;
    }


}
