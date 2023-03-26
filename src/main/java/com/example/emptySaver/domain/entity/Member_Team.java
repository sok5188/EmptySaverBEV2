package com.example.emptySaver.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
public class Member_Team {
    @Id@GeneratedValue
    private Long id;


    //private List<Team> team;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Team team;

    @ManyToOne(fetch =FetchType.LAZY)
    @JoinColumn(name = "member_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Member member;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Member owner;
}
