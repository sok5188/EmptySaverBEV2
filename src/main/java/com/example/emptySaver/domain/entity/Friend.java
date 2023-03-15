package com.example.emptySaver.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.util.List;

@Document(collection = "friend")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Friend {
    @Id
    @GeneratedValue
    private Long id;

    @DocumentReference
    private Member host_member;

    @DocumentReference
    private List<Member> friend_members;
}
