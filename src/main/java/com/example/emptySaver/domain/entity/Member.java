package com.example.emptySaver.domain.entity;

import com.example.emptySaver.utils.Constant;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@ToString
@DynamicUpdate
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = "email",name = Constant.Constraint.EMAIL_UNIQUE)})
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
    //private String phone;
    private String email;
    @Enumerated(EnumType.STRING)
    private MemberRole role;
    private String refreshToken;
    private String fcmToken;

    @ToString.Exclude
    @OneToMany(mappedBy = "member")
    private List<MemberTeam> memberTeam =new ArrayList<>();

    @ToString.Exclude
    @OneToMany(mappedBy = "member")
    private List<MemberInterest> memberInterests =new ArrayList<>();

    @ToString.Exclude
    @OneToMany(mappedBy = "friendMember")
    private List<Friend> friends=new ArrayList<>();
    @Builder(builderClassName = "InitUser",builderMethodName = "init")
    public Member(String username, String password, String classOf, String name,String nickname, String email){
        this.username = username;
        this.password = password;
        this.classOf = classOf;
        this.name =name;
        this.nickname=nickname;
        this.email=email;
        this.role=MemberRole.USER;
    }

    /*
    @ToString.Exclude
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "time_table_id")*/
    @OneToOne(mappedBy = "member", cascade = CascadeType.REMOVE)
    private Time_Table timeTable;

    @OneToMany(mappedBy = "member")
    @ToString.Exclude
    private List<Comment> comments=new ArrayList<>();
}
