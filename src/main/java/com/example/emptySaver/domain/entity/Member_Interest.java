package com.example.emptySaver.domain.entity;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

import java.util.List;

@Document(collection = "member_interest")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Member_Interest {
    @Id@GeneratedValue
    private Long id;
    @DocumentReference
    private Member member;

    @DocumentReference
    private List<Interest> interests;

}
