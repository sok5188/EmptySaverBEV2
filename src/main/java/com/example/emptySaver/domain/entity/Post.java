package com.example.emptySaver.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Post {
    @Id
    @GeneratedValue
    @Column(name = "post_id")
    private Long id;

    @Column(nullable = false)
    private String title;
    @Column(nullable = false, length = 50000)
    private String content;
    private String memberName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Team team;

    private ZonedDateTime date;

    @OneToMany(mappedBy = "post")
    private List<Comment> commentList = new ArrayList<>();

    @Builder(builderMethodName = "init")
    public Post(String memberName, Team team,String title, String content){
        this.memberName=memberName;
        this.team=team;
        this.title=title;
        this.content=content;
        this.date=LocalDateTime.now().atZone(ZoneId.of("Asia/Seoul"));
        team.getTeamPosts().add(this);
    }

}
