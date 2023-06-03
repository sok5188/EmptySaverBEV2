package com.example.emptySaver.domain.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

public class PostDto {
    @Data
    @Builder
    public static class PostAddReq{
        private String title;
        private String content;
        private Long groupId;
    }
    @Data
    @Builder
    public static class PostUpdateReq{
        private String title;
        private String content;
        private Long postId;
    }

    @Data
    @Builder
    public static class PostDetailRes{
        private String title;
        private String content;
        private Long postId;
        private ZonedDateTime dateTime;
        private List<CommentDto.CommentRes> comments;
    }

    @Data
    @Builder
    public static class SimplePostInfo{
        private String title;
        private Long postId;
    }
}
