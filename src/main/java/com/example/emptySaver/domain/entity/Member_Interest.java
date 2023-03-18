package com.example.emptySaver.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
public class Member_Interest {
    @Id@GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn
    private Member member;


    @ManyToOne
    @JoinColumn
    private Interest interest;

}
