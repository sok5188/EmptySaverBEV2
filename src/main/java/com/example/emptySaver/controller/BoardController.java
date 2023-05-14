package com.example.emptySaver.controller;

import com.example.emptySaver.domain.dto.CommentDto;
import com.example.emptySaver.domain.dto.PostDto;
import com.example.emptySaver.service.BoardService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/board")
public class BoardController {
    private final BoardService boardService;
    @PutMapping("/updateComment")
    @Operation(summary = "댓글을 수정하는 API", description = "해당 댓글의 id와 수정할 text를 받아 댓글 내용을 수정하는 API")
    public ResponseEntity<String> updateComment(@RequestBody CommentDto.CommentUpdateReq req){
        boardService.updateComment(req);
        return new ResponseEntity<>("Comment Updated", HttpStatus.OK);
    }
    @DeleteMapping("/deleteComment/{commentId}")
    @Operation(summary = "댓글을 삭제하는 API", description = "pathVariable로 commentID를 받아 본인 혹은 그룹장인 경우 해당 댓글을 삭제하는 API")
    public ResponseEntity<String> deleteComment(@PathVariable Long commentId){
        boardService.deleteComment(commentId);
        return new ResponseEntity<>("Comment Deleted",HttpStatus.OK);
    }
    @PostMapping("/addGroupComment")
    @Operation(summary = "그룹 상세 조회 페이지에 댓글을 남기는 API",
            description = "그룹을 검색하여 상세조회한 경우 해당 그룹의 글에 댓글을 남기는 API(대댓글인 경우 부모 댓글의 id값을 줘야 한다 / 그렇지 않으면 -1주세용)")
    public ResponseEntity<String> addComment(@RequestBody CommentDto.CommentAddReq commentAddReq){
        boardService.addCommentToDetail(commentAddReq);
        return new ResponseEntity<>("Comment Saved",HttpStatus.OK);
    }
    // 여기까지 체크 완.
    @PostMapping("/addPostComment")
    @Operation(summary = "공지사항에 댓글을 남기는 API", description = "그룹 내 공지사항에 댓글을 남기는 API(대댓글인 경우 부모 댓글의 id값을 줘야 한다 / 그렇지 않으면 -1주세용)")
    public ResponseEntity<String> addCommentToPost(@RequestBody CommentDto.PostCommentAddReq req){
        boardService.addCommentToPost(req);
        return new ResponseEntity<>("Comment Saved",HttpStatus.OK);
    }

    @PostMapping("/makePost")
    @Operation(summary = "글을 작성하는 API", description = "그룹 내 공지사항을 생성하는 API")
    public ResponseEntity<String> makePost(@RequestBody PostDto.PostAddReq req){
        boardService.addPost(req);
        return new ResponseEntity<>("Post created",HttpStatus.OK);
    }
    @DeleteMapping("/deletePost/{postId}")
    @Operation(summary = "게시글 삭제", description = "그룹 내 공지사항을 삭제하는 API (작성자인 그룹장만 가능)")
    public ResponseEntity<String> deletePost(@PathVariable Long postId){
        boardService.deletePost(postId);
        return new ResponseEntity<>("Post deleted",HttpStatus.OK);
    }
    @PutMapping("/updatePost")
    @Operation(summary = "게시글 수정", description = "그룹 내 공지사항을 수정하는 API (작성자인 그룹장만 가능) ," +
            " !! 수정하고 싶은 부분(제목,내용)만 값을 채워서 보내면 된다!! 만약 제목을 수정하고 싶지 않다면 그냥 제목변수없이 보내면 됨 (아니면 기존 값으로 보내도 됨)")
    public ResponseEntity<String> updatePost(@RequestBody PostDto.PostUpdateReq req){
        boardService.updatePost(req);
        return new ResponseEntity<>("Post updated",HttpStatus.OK);
    }
    @GetMapping("/getPost/{postId}")
    @Operation(summary = "게시글 조회", description = "그룹 내 공지사항(게시글)을 조회하는 API(그룹 구성원만 가능)")
    public ResponseEntity<PostDto.PostDetailRes> getPostDetail(@PathVariable Long postId){
        PostDto.PostDetailRes postDetail = boardService.getPostDetail(postId);
        return new ResponseEntity<>(postDetail,HttpStatus.OK);
    }
    @GetMapping("/getPostList/{groupId}")
    @Operation(summary = "공지사항 목록 조회", description = "그룹 내 공지사항 목록을 반환하는 API(그룹 구성원만 가능)")
    public ResponseEntity<List<PostDto.SimplePostInfo>> getPostList(@PathVariable Long groupId){
        List<PostDto.SimplePostInfo> postList = boardService.getPostList(groupId);
        return new ResponseEntity<>(postList,HttpStatus.OK);
    }
}
