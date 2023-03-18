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
    private Long id;

    private String username;
    private String password;
    private String classOf;
    private String name;
    private String nickname;
    private String phone;
    private String email;
    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToMany(mappedBy = "member")
    private List<Member_Team> member_team;
//    @ManyToOne
//    @JoinColumn(name = "host_member_id",referencedColumnName = "member_id")
//    private Member host_member;

//    @OneToMany(mappedBy = "host_member")
//    private List<Friend> friends;

    @OneToMany(mappedBy = "member")
    private List<Member_Interest> member_interests;

    @OneToMany(mappedBy = "friend_member")
    private List<Friend> friends;
}
