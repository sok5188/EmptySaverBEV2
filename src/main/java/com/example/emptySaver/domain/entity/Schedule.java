package com.example.emptySaver.domain.entity;

import jakarta.persistence.*;
import lombok.*;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Builder
@ToString
@Inheritance(strategy = InheritanceType.JOINED) // 하위 클래스는 그 그 클래스의 데이터만 저장
@DiscriminatorColumn // 하위 테이블의 구분 컬럼 생성 default = DTYPE
public class Schedule {
    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private String body;

    @ManyToOne
    @JoinColumn(name = "time_table_id")
    @ToString.Exclude
    private Time_Table timeTable;
}
