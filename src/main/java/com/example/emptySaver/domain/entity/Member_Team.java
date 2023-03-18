package com.example.emptySaver.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
public class Member_Team {
    @Id@GeneratedValue
    private Long id;


    //private List<Team> team;
    @ManyToOne
    @JoinColumn
    private Team team;

    @ManyToOne
    @JoinColumn
    private Member member;

}
