package com.example.emptySaver.domain.entity;

import jakarta.persistence.Entity;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
//@Builder
@ToString
public class Periodic_Schedule extends Schedule{
    private LocalDateTime start;
    private LocalDateTime end;
    private int[][] weekScheduleData;
}
