package com.example.emptySaver.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "time_table")
@Builder
public class Time_Table {
    @Id
    @GeneratedValue
    private Long id;
    private String title;

    //외래키 저장을 상대에게 위임 -> 상대는 @joinColumn에 외래키 저장
    @OneToMany(mappedBy = "timeTable", fetch = FetchType.EAGER, cascade = CascadeType.ALL) //casecade all로 이 table사라지면 일정도 같이 제거됨
    @ToString.Exclude
    private List<Schedule> scheduleList = new ArrayList<>();
}
