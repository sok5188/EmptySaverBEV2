package com.example.emptySaver.domain.entity;

import com.example.emptySaver.domain.entity.category.Category;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Builder
@ToString
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS) // 하위 클래스는 그 그 클래스의 데이터만 저장
//@DiscriminatorColumn // 하위 테이블의 구분 컬럼 생성 default = DTYPE
public class Schedule {
    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "ScheduleIdGenerator")
    @TableGenerator(table = "SEQUENCES", name = "ScheduleIdGenerator")
    @Column(name = "schedule_id")
    private Long id;
    private String name;
    private String body;

    @ColumnDefault("false")
    private boolean publicType;

    @ColumnDefault("false")
    private boolean groupType;      //그룹에 의해 추가된 경우
    private Long groupId;
    private String groupName;
    private Long originScheduleId;  //그룹 스케줄 원본의 id

    private String category;    //카테고리 구분은 일단 임시로 string으로
    private String subCategory; //sub카테고리

    @ManyToOne
    @JoinColumn(name = "time_table_id")
    @ToString.Exclude
    private Time_Table timeTable;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
