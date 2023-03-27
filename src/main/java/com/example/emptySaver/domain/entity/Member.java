package com.example.emptySaver.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@ToString
public class Member {
    @Id
    @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    private String username;
    private String password;
    private String classOf;
    private String name;
    private String nickname;
    private String phone;
    private String email;
    @Enumerated(EnumType.STRING)
    private MemberRole role;

    @ToString.Exclude
    @OneToMany(mappedBy = "member")
    private List<MemberTeam> memberTeam =new ArrayList<>();

    @ToString.Exclude
    @OneToMany(mappedBy = "member")
    private List<MemberInterest> memberInterests =new ArrayList<>();

    @ToString.Exclude
    @OneToMany(mappedBy = "friendMember")
    private List<Friend> friends=new ArrayList<>();
}
