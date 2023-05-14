package com.example.emptySaver.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Comment {
    @Id
    @GeneratedValue
    @Column(name = "comment_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id",nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Member member;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Team team;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Post post;

    @Column(nullable = false)
    private String text;
    @Column(nullable = false)
    private LocalDateTime date;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Comment parentComment;
    @OneToMany(mappedBy = "parentComment", orphanRemoval = true)
    private List<Comment> childComment = new ArrayList<>();

    @Builder(builderMethodName = "init_detail")
    public Comment(Member member, Team team, String text){
        //detail댓글이면 post정보 x
        this.member=member;
        this.team=team;
        this.text=text;
        this.date=LocalDateTime.now();
        member.getComments().add(this);
    }
    @Builder(builderMethodName = "init_post")
    public Comment(Member member, String text,Post post){
        //post의 댓글이면 team정보 X
        this.member=member;
        this.text=text;
        this.date=LocalDateTime.now();
        this.post=post;
        member.getComments().add(this);
        post.getCommentList().add(this);
    }
    public void makeRelation(Comment child, Comment parent){
        child.parentComment=parent;
        parent.childComment.add(child);
    }
    public void makePostRelation(Comment child, Comment parent, Post post){
        child.parentComment=parent;
        parent.childComment.add(child);
        child.post=post;
        post.getCommentList().add(child);
    }
    public void makePostRelation(Comment child, Post post){
        child.post=post;
        post.getCommentList().add(child);
    }

}
