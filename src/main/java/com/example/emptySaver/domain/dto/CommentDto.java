package com.example.emptySaver.domain.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

public class CommentDto {
    @Data
    @Builder
    public static class CommentAddReq {
        private Long groupId;
        private Long parentCommentId;
        private String text;

    }
    @Data
    @Builder
    public static class PostCommentAddReq {
        private Long groupId;
        private Long parentCommentId;
        private Long postId;
        private String text;

    }
    @Data
    @Builder
    public static class CommentRes{
        //부모 comment기준으로 날리자.
        private CommentInfo parent;
        private List<CommentInfo> childList;
    }

    @Data
    @Builder
    public static class CommentInfo{
        private Long commentId;
        private String text;
        private LocalDateTime dateTime;
        private Boolean isOwner;
        private String writerName;
        private Boolean amIWriter;
    }

    @Data
    @Builder
    public static class CommentUpdateReq{
        private Long commentId;
        private String text;
    }
}
