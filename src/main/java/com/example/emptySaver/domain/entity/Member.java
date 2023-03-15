package com.example.emptySaver.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.util.List;

@Document(collection = "member")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
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

    @DocumentReference
    private List<Group> groups;

}
