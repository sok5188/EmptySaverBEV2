package com.example.emptySaver.domain.entity;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;

@Document(collection = "interest")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Interest {
    @Id@GeneratedValue
    private Long id;

    private String interestName;

    @DocumentReference
    private Category category;
}
