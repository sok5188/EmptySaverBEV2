package com.example.emptySaver.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;


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

    @ColumnDefault("false")
    private boolean publicType;

    @ColumnDefault("false")
    private boolean groupType;      //그룹에 의해 추가된 경우
    private Long originScheduleId;  //그룹 스케줄 원본의 id

    @ManyToOne
    @JoinColumn(name = "time_table_id")
    @ToString.Exclude
    private Time_Table timeTable;
}
