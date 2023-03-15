package com.example.emptySaver.domain.entity;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.util.List;

@Document(collection = "group")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Group {
    @Id@GeneratedValue
    private Long id;
    @DocumentReference
    private Member owner;

    private String name;

    @DocumentReference
    private List<Member> members;
}
