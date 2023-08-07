package com.example.emptySaver.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
public class MemberTeam {
    @Id@GeneratedValue
    private Long id;
//    @Version
//    private Integer version;

    private LocalDateTime joinDate;

    //private List<Team> team;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Team team;

    @ManyToOne(fetch =FetchType.LAZY)
    @JoinColumn(name = "member_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Member member;

    private boolean isBelong;

    private String relationSubject;

    @Transactional
    public void initMemberTeam(Member member, Team team, Member owner){
        this.member=member;
        this.team=team;
        member.getMemberTeam().add(this);
        team.getTeamMembers().add(this);
        this.joinDate=ZonedDateTime.now(ZoneId.of("Asia/Seoul")).toLocalDateTime();
        isBelong=false;
    }

    public void addMemberToTeam(){
        this.isBelong=true;
    }
}
