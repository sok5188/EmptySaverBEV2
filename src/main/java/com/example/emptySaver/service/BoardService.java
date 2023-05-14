package com.example.emptySaver.service;

import com.example.emptySaver.domain.dto.CommentDto;
import com.example.emptySaver.domain.dto.PostDto;
import com.example.emptySaver.domain.entity.*;
import com.example.emptySaver.errorHandler.BaseException;
import com.example.emptySaver.errorHandler.BaseResponseStatus;
import com.example.emptySaver.repository.CommentRepository;
import com.example.emptySaver.repository.MemberTeamRepository;
import com.example.emptySaver.repository.PostRepository;
import com.example.emptySaver.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class BoardService {
    private final MemberService memberService;
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final TeamRepository teamRepository;
    private final MemberTeamRepository memberTeamRepository;
    @Transactional
    public void deleteComment(Long commentId){
        Comment comment = getComment(commentId);
        if(!checkCommentMember(comment)&&!comment.getTeam().getOwner().equals(memberService.getMember()))
            throw new BaseException(BaseResponseStatus.NOT_ALLOWED);
        commentRepository.delete(comment);
    }
    @Transactional
    public void updateComment(CommentDto.CommentUpdateReq req){
        Comment comment = getComment(req.getCommentId());
        if(!checkCommentMember(comment))
            throw new BaseException(BaseResponseStatus.NOT_ALLOWED);
        comment.setText(req.getText());
    }
    private boolean checkCommentMember(Comment comment){
        //현재 유저가 보낸건지 확인
        Member member = memberService.getMember();
        return comment.getMember().equals(member);
    }
    private Comment getComment(Long commentId){
        return commentRepository.findById(commentId).orElseThrow(() -> new BaseException(BaseResponseStatus.INVALID_COMMENT_ID));
    }
    @Transactional
    public void addCommentToDetail(CommentDto.CommentAddReq commentAddReq){
        Member member = memberService.getMember();
        Team team = teamRepository.findById(commentAddReq.getGroupId()).orElseThrow(()->new BaseException(BaseResponseStatus.INVALID_TEAM_ID));
        if(commentAddReq.getParentCommentId()>0L){
            //parent 존재 -> 대댓글이다.
            Comment parent = this.getComment(commentAddReq.getParentCommentId());
            if(parent.getParentComment()!=null)
                throw new BaseException(BaseResponseStatus.NOT_ALLOWED);
            if(parent.getTeam()==null||parent.getPost()!=null)
                throw new BaseException(BaseResponseStatus.NOT_ALLOWED);
            Comment build = Comment.init_detail().member(member).team(team).text(commentAddReq.getText()).build();
            build.makeRelation(build,parent);
            commentRepository.save(build);
        }else{
            Comment build = Comment.init_detail().member(member).team(team).text(commentAddReq.getText()).build();
            commentRepository.save(build);
        }
    }
    @Transactional
    public void addCommentToPost(CommentDto.PostCommentAddReq req){
        Member member = memberService.getMember();
        Post post = getPost(req.getPostId());
        if(req.getParentCommentId()>0){
            //parent 존재 -> 대댓글이다.
            Comment parent = this.getComment(req.getParentCommentId());
            if(parent.getParentComment()!=null)
                throw new BaseException(BaseResponseStatus.NOT_ALLOWED);
            if(parent.getTeam()!=null||parent.getPost()==null)
                throw new BaseException(BaseResponseStatus.NOT_ALLOWED);
            Comment build = Comment.init_post().member(member).post(post).text(req.getText()).build();
            build.makePostRelation(build,parent,post);
            commentRepository.save(build);
        }else{
            Comment build = Comment.init_post().member(member).text(req.getText()).post(post).build();
            build.makePostRelation(build,post);
            commentRepository.save(build);
        }
    }
    private Post getPost(Long id){
        return postRepository.findById(id).orElseThrow(()->new BaseException(BaseResponseStatus.INVALID_POST_ID));
    }
    private Team getTeam(Long id){
        return teamRepository.findById(id).orElseThrow(()->new BaseException(BaseResponseStatus.INVALID_TEAM_ID));
    }
    public List<CommentDto.CommentRes> getDetailComments(Long groupId){
        Team team = getTeam(groupId);
        List<Comment> byTeam = commentRepository.findByTeam(team);
        List<CommentDto.CommentRes> result = new ArrayList<>();
        byTeam.stream().forEach(comment -> {
            if(comment.getParentComment()==null){
                CommentDto.CommentInfo parent = CommentDto.CommentInfo.builder().commentId(comment.getId()).text(comment.getText()).dateTime(comment.getDate())
                        .isOwner(comment.getMember().equals(team.getOwner())).build();
                List<CommentDto.CommentInfo> childList=new ArrayList<>();
                comment.getChildComment().forEach(child->childList.add(
                        CommentDto.CommentInfo.builder().commentId(child.getId()).text(child.getText())
                                .dateTime(child.getDate()).isOwner(child.getMember().equals(team.getOwner())).build()
                ));
                result.add(CommentDto.CommentRes.builder().parent(parent).childList(childList).build());
            }
        });
        return result;
    }
    public PostDto.PostDetailRes getPostDetail(Long postId){
        Post post=postRepository.findWithTeamById(postId).orElseThrow(()->new BaseException(BaseResponseStatus.INVALID_POST_ID));
        if(!this.checkBelong(post.getTeam().getId()))
            throw new BaseException(BaseResponseStatus.NOT_BELONG_ERROR);
        List<Comment> byPost = commentRepository.findByPost(post);
        log.info("byPost size:"+byPost.size());
        List<CommentDto.CommentRes> result = new ArrayList<>();
        byPost.stream().forEach(comment -> {
            log.info("now comment:"+comment.getText());
            log.info("is parent?:"+(comment.getParentComment()==null));
            if(comment.getParentComment()==null){
                CommentDto.CommentInfo parent = CommentDto.CommentInfo.builder().commentId(comment.getId()).text(comment.getText()).dateTime(comment.getDate())
                        .isOwner(comment.getMember().equals(post.getMember())).build();
                List<CommentDto.CommentInfo> childList=new ArrayList<>();
                comment.getChildComment().forEach(child->childList.add(
                        CommentDto.CommentInfo.builder().commentId(child.getId()).text(child.getText())
                                .dateTime(child.getDate()).isOwner(child.getMember().equals(post.getMember())).build()
                ));
                result.add(CommentDto.CommentRes.builder().parent(parent).childList(childList).build());
            }
        });
        log.info("result Size:"+result.size());
        PostDto.PostDetailRes response = PostDto.PostDetailRes.builder().title(post.getTitle()).content(post.getContent()).postId(post.getId())
                .dateTime(post.getDate()).comments(result).build();
        return response;
    }

    @Transactional
    public void addPost(PostDto.PostAddReq req){
        Member member = memberService.getMember();
        Team team = getTeam(req.getGroupId());
        //TODO: 지연로딩 될 지점 (아마)
        if(!team.getOwner().equals(member))
            throw new BaseException(BaseResponseStatus.NOT_ALLOWED);
        Post build = Post.init().member(member).team(team).title(req.getTitle()).content(req.getContent()).build();
        postRepository.save(build);
    }
    @Transactional
    public void deletePost(Long postId){
        Member member = memberService.getMember();
        Post post = postRepository.findWithMemberById(postId).orElseThrow(() -> new BaseException(BaseResponseStatus.INVALID_POST_ID));
        if(!post.getMember().equals(member))
            throw new BaseException(BaseResponseStatus.NOT_ALLOWED);
        postRepository.delete(post);
    }
    @Transactional
    public void updatePost(PostDto.PostUpdateReq req){
        Member member = memberService.getMember();
        Post post = postRepository.findWithMemberById(req.getPostId()).orElseThrow(() -> new BaseException(BaseResponseStatus.INVALID_POST_ID));
        if(!post.getMember().equals(member))
            throw new BaseException(BaseResponseStatus.NOT_ALLOWED);
        if(req.getTitle()!=null)
            post.setTitle(req.getTitle());
        if(req.getContent()!=null)
            post.setContent(req.getContent());
        //TODO : 수정일 표시기능..? 뭐 너무 부차적이니 pass..
    }

    public List<PostDto.SimplePostInfo> getPostList(Long groupId){
        if(!this.checkBelong(groupId))
            throw new BaseException(BaseResponseStatus.NOT_BELONG_ERROR);
        Team team = getTeam(groupId);
        List<Post> byTeam = postRepository.findByTeam(team);
        return byTeam.stream().map(post-> PostDto.SimplePostInfo.builder().postId(post.getId()).title(post.getTitle()).build())
                .collect(Collectors.toList());
    }
    public boolean checkBelong(Long groupId){
        Member member = memberService.getMember();
        Team team = this.getTeam(groupId);
        Optional<MemberTeam> opt = memberTeamRepository.findFirstByMemberAndTeam(member, team);
        return opt.isPresent()&&opt.get().isBelong();
    }

}
