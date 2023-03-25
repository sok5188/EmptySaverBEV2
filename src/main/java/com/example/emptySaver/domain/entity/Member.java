package com.example.emptySaver.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
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

    @OneToMany(mappedBy = "member")
    private List<Member_Team> member_team;

    @OneToMany(mappedBy = "member")
    private List<Member_Interest> member_interests;

    @OneToMany(mappedBy = "friend_member")
    private List<Friend> friends;
}
