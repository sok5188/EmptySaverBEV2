package com.example.emptySaver.domain.entity;

import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import lombok.*;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
//@Builder
@ToString
@Inheritance(strategy = InheritanceType.JOINED) // 하위 클래스는 그 그 클래스의 데이터만 저장
@DiscriminatorColumn // 하위 테이블의 구분 컬럼 생성 default = DTYPE
public class Periodic_Schedule extends Schedule{
    private LocalDateTime start;
    private LocalDateTime end;
    private long[] weekScheduleData;
}
