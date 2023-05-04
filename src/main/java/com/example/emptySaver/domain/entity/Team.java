package com.example.emptySaver.domain.entity;

import com.example.emptySaver.domain.entity.category.Category;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
public class Team {
    @Id@GeneratedValue
    @Column(name = "team_id")
    private Long id;
    private String name;
    private String oneLineInfo;
    private String description;

    private Long maxMember;
    private boolean isPublic;
    @Builder
    public Team(String name, String oneLineInfo, String description
            , Long maxMember, boolean isPublic, Category category, Member owner) {
        this.name = name;
        this.oneLineInfo = oneLineInfo;
        this.description = description;
        this.maxMember = maxMember;
        this.isPublic = isPublic;
        this.category=category;
        this.owner = owner;
        category.getCategoryTeamList().add(this);
        this.createTime=LocalDateTime.now();
    }

    //TODO: 활동 일정? 스케쥴같은 정보가 내장되어있거나 매핑으로 찾을 수 있어야 한다.

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @OneToMany(mappedBy = "team")
    @ToString.Exclude
    private List<MemberTeam> teamMembers=new ArrayList<>();
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    //@OnDelete(action = OnDeleteAction.CASCADE)
    private Member owner;

    private LocalDateTime createTime;
}
