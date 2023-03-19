package com.example.emptySaver.domain.entity;

import jakarta.persistence.Entity;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
//@Builder
@ToString
public class Periodic_Schedule extends Schedule{
    private String date;
}
