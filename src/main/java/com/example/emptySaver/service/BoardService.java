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
import io.jsonwebtoken.lang.Collections;
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
    private final FCMService fcmService;
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
        Team team = teamRepository.findWithOwnerById(commentAddReq.getGroupId()).orElseThrow(()->new BaseException(BaseResponseStatus.INVALID_TEAM_ID));
        if(commentAddReq.getParentCommentId()>0L){
            //parent 존재 -> 대댓글이다.
            Comment parent = commentRepository.findWithMemberAndTeamById(commentAddReq.getParentCommentId())
                    .orElseThrow(()->new BaseException(BaseResponseStatus.INVALID_COMMENT_ID));
            if(parent.getParentComment()!=null)
                throw new BaseException(BaseResponseStatus.NOT_ALLOWED);
            if(parent.getTeam()==null||parent.getPost()!=null)
                throw new BaseException(BaseResponseStatus.NOT_ALLOWED);
            Comment build = Comment.init_detail().member(member).team(team).text(commentAddReq.getText()).build();
            build.makeRelation(build,parent);
            commentRepository.save(build);

            //지연로딩 발생할거임
            //부모 댓글이 있고 그 작성자가 그룹장도 아니고 자기 자신도 아닌 경우 해당 작성자에게 알림을 보낸다.
            // 부모 작성자가 그룹장이면 == 어차피 밑에서 통합처리함
            if(!parent.getMember().equals(team.getOwner())&&!parent.getMember().equals(member)) {
                //부모 댓글 작성자가 그룹장이 아닌 경우 -> 부모 댓글 작성자에게 답글 알림을 보낸다.
                fcmService.sendMessageToMember(parent.getMember().getId(), team.getName() + "그룹 소개 페이지에 댓글이 달렸습니다"
                        , (team.isAnonymous() ? member.getNickname() : member.getName()) + " : " + build.getText()
                        , "groupDetail", "group", String.valueOf(team.getId()));
            }
        }else{
            Comment build = Comment.init_detail().member(member).team(team).text(commentAddReq.getText()).build();
            commentRepository.save(build);
        }
        //댓글 작성자가 그룹장인 경우 -> 부모 댓글이 존재하고 그 부모 작성자가 그룹장이 아닌 경우에만 해당 작성자에게 알림
        // 댓글 작성자가 그룹장이 아니면 -> 마찬가지로
        if(!member.equals(team.getOwner()))
            fcmService.sendMessageToMember(team.getOwner().getId(),team.getName()+"그룹 소개 페이지에 댓글이 달렸습니다"
                    , (team.isAnonymous()? member.getNickname(): member.getName())+ " : "+commentAddReq.getText()
                    ,"groupDetail","group",String.valueOf(team.getId()));
    }
    @Transactional
    public void addCommentToPost(CommentDto.PostCommentAddReq req){
        Member member = memberService.getMember();
        Post post = getPost(req.getPostId());
        if(req.getParentCommentId()>0){
            //parent 존재 -> 대댓글이다.
            Comment parent = commentRepository.findWithMemberAndPostById(req.getParentCommentId())
                    .orElseThrow(()->new BaseException(BaseResponseStatus.INVALID_COMMENT_ID));
            if(parent.getParentComment()!=null)
                throw new BaseException(BaseResponseStatus.NOT_ALLOWED);
            if(parent.getTeam()!=null||parent.getPost()==null)
                throw new BaseException(BaseResponseStatus.NOT_ALLOWED);
            Comment build = Comment.init_post().member(member).post(post).text(req.getText()).build();
            build.makePostRelation(build,parent,post);
            commentRepository.save(build);
            if(!parent.getMember().equals(post.getTeam().getOwner())&&!parent.getMember().equals(member)) {
                //부모 댓글 작성자가 그룹장이 아닌 경우 + 자신이 아닌경우-> 부모 댓글 작성자에게 답글 알림을 보낸다.
                fcmService.sendMessageToMember(post.getTeam().getOwner().getId(),post.getTitle()+"글에 댓글이 달렸습니다"
                        , (post.getTeam().isAnonymous()? member.getNickname(): member.getName())+ " : "+req.getText()
                        ,"post","group",String.valueOf(post.getTeam().getId()),"post", String.valueOf(post.getId()));
            }
        }else{
            Comment build = Comment.init_post().member(member).text(req.getText()).post(post).build();
            build.makePostRelation(build,post);
            commentRepository.save(build);
        }
        //게시글의 팀의 그룹장과 현재 댓글을 남긴 회원이 같지 않다면 해당 그룹의 그룹장에게 알림을 보낸다.
        //TODO: 근데 여기선 그룹 id랑 post id를 같이 줘야 해당 그룹의 post로 이동할 수 있을 것 같은디ㅣ...
        if(!post.getTeam().getOwner().equals(member)){
            fcmService.sendMessageToMember(post.getTeam().getOwner().getId(),post.getTitle()+"글에 댓글이 달렸습니다"
                    , (post.getTeam().isAnonymous()? member.getNickname(): member.getName())+ " : "+req.getText()
                    ,"post","group",String.valueOf(post.getTeam().getId()),"post", String.valueOf(post.getId()));
        }
    }
    private Post getPost(Long id){
        return postRepository.findWithTeamById(id).orElseThrow(()->new BaseException(BaseResponseStatus.INVALID_POST_ID));
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
                        .isOwner(comment.getMember().equals(team.getOwner()))
                        .writerName(team.isAnonymous()?comment.getMember().getNickname():comment.getMember().getName())
                        .build();
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
                        .isOwner(comment.getMember().equals(post.getTeam().getOwner()))
                        .writerName(post.getTeam().isAnonymous()?comment.getMember().getNickname():comment.getMember().getName())
                        .build();
                List<CommentDto.CommentInfo> childList=new ArrayList<>();
                comment.getChildComment().forEach(child->childList.add(
                        CommentDto.CommentInfo.builder().commentId(child.getId()).text(child.getText())
                                .dateTime(child.getDate()).isOwner(child.getMember()
                                        .equals(post.getTeam().getOwner())).build()
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
        log.info("member Name:"+member.getName());
        Team team = getTeam(req.getGroupId());
        log.info("team name:"+team.getName());
        //TODO: 지연로딩 될 지점 (아마)
        if(!team.getOwner().equals(member))
            throw new BaseException(BaseResponseStatus.NOT_ALLOWED);
        Post build = Post.init().memberName(member.getName()).team(team).title(req.getTitle()).content(req.getContent()).build();
        postRepository.save(build);
        fcmService.sendMessageToMemberList(team.getTeamMembers().stream().map( memberTeam -> memberTeam.getMember().getId()).collect(Collectors.toList())
                , team.getName()+" 그룹에 새로운 공지사항이  등록되었습니다.", req.getContent(), "notification","group", String.valueOf(team.getId()));
    }
    @Transactional
    public void deletePost(Long postId){
        Member member = memberService.getMember();
        Post post = postRepository.findWithTeamById(postId).orElseThrow(() -> new BaseException(BaseResponseStatus.INVALID_POST_ID));
        if(!post.getTeam().getOwner().equals(member))
            throw new BaseException(BaseResponseStatus.NOT_ALLOWED);
        postRepository.delete(post);
    }
    @Transactional
    public void updatePost(PostDto.PostUpdateReq req){
        Member member = memberService.getMember();
        Post post = postRepository.findWithTeamById(req.getPostId()).orElseThrow(() -> new BaseException(BaseResponseStatus.INVALID_POST_ID));
        if(!post.getTeam().getOwner().equals(member))
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
