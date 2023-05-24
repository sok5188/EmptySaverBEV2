package com.example.emptySaver.domain.entity;

import jakarta.persistence.Entity;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Setter
@Entity
@ToString
public class Non_Periodic_Schedule extends Schedule{
    //private LocalDateTime startTime;
    //private LocalDateTime endTime;
}