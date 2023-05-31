package com.example.emptySaver.domain.entity;

import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import lombok.*;


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
    private long[] weekScheduleData;

    static public Periodic_Schedule copySchedule(Periodic_Schedule schedule){
        Periodic_Schedule periodicSchedule = new Periodic_Schedule();
        periodicSchedule.setWeekScheduleData(schedule.getWeekScheduleData());

        periodicSchedule.setName(schedule.getName());
        periodicSchedule.setBody(schedule.getBody());
        periodicSchedule.setPublicType(false);

        periodicSchedule.setGroupType(schedule.isGroupType());
        periodicSchedule.setGroupId(schedule.getGroupId());
        periodicSchedule.setGroupName(schedule.getGroupName());
        periodicSchedule.setOriginScheduleId(schedule.getId());

        periodicSchedule.setCategory(schedule.getCategory());
        periodicSchedule.setSubCategory(schedule.getSubCategory());

        periodicSchedule.setStartTime(schedule.getStartTime());
        periodicSchedule.setEndTime(schedule.getEndTime());

        return periodicSchedule;
    }
}
