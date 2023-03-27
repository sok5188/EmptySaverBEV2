package com.example.emptySaver.domain.entity;

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
@ToString
public class Team {
    @Id@GeneratedValue
    @Column(name = "team_id")
    private Long id;
    private String name;

    @OneToMany(mappedBy = "team")
    @ToString.Exclude
    private List<MemberTeam> teamMembers=new ArrayList<>();

    private LocalDateTime createTime;
}
