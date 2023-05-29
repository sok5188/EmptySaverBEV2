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
    static public Non_Periodic_Schedule copySchedule(Non_Periodic_Schedule schedule){
        Non_Periodic_Schedule nonPeriodicSchedule = new Non_Periodic_Schedule();

        nonPeriodicSchedule.setName(schedule.getName());
        nonPeriodicSchedule.setBody(schedule.getBody());
        nonPeriodicSchedule.setPublicType(false);

        nonPeriodicSchedule.setGroupType(schedule.isGroupType());
        nonPeriodicSchedule.setGroupId(schedule.getGroupId());
        nonPeriodicSchedule.setGroupName(schedule.getGroupName());
        nonPeriodicSchedule.setOriginScheduleId(schedule.getId());

        nonPeriodicSchedule.setCategory(schedule.getCategory());

        nonPeriodicSchedule.setStartTime(schedule.getStartTime());
        nonPeriodicSchedule.setEndTime(schedule.getEndTime());

        return nonPeriodicSchedule;
    }
}