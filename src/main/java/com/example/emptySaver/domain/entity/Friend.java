package com.example.emptySaver.domain.entity;

import jakarta.persistence.*;
import lombok.*;
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
public class Friend extends Member{
    @ManyToOne
    @JoinColumn
    private Member friend_member;

}
