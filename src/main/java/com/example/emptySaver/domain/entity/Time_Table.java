package com.example.emptySaver.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Entity
@Builder
public class Time_Table {
    @Id
    @GeneratedValue
    private Long id;
    private String title;

    @OneToOne(mappedBy = "timeTable")
    private Member member;

    @OneToOne(mappedBy = "timeTable")
    private Team team;

    //외래키 저장을 상대에게 위임 -> 상대는 @joinColumn에 외래키 저장
    @Builder.Default
    @OneToMany(mappedBy = "timeTable", fetch = FetchType.LAZY, cascade = CascadeType.ALL,orphanRemoval = true) //casecade all로 이 table사라지면 일정도 같이 제거됨
    @ToString.Exclude
    private List<Schedule> scheduleList = new ArrayList<>();

    private long[] weekScheduleData = {0,0,0,0,0,0,0};

    public void calcAllWeekScheduleData(){
        this.weekScheduleData = new long[]{0,0,0,0,0,0,0};   //0으로 init 후 재계산

        for (Schedule schedule: this.scheduleList) {
            if (!(schedule instanceof  Periodic_Schedule))
                continue;
            Periodic_Schedule periodicSchedule = (Periodic_Schedule)schedule;
            addWeekScheduleData(periodicSchedule.getWeekScheduleData());
        }
        /*
        scheduleList.stream().forEach(schedule -> {
            Periodic_Schedule periodicSchedule = (Periodic_Schedule)schedule;
            addWeekScheduleData(periodicSchedule.getWeekScheduleData());
        });*/
    }

    private void addWeekScheduleData(long[] otherScheduleData){
        for (int i=0; i<otherScheduleData.length ; ++i)
            this.weekScheduleData[i] |= otherScheduleData[i];
    }

    public List<Non_Periodic_Schedule> getNonPeriodicScheduleInBound(final LocalDateTime start, final LocalDateTime end){
        LocalDateTime startTime = start.minusMinutes(1);
        LocalDateTime endTime = end.plusMinutes(1);

        List<Non_Periodic_Schedule> nonPeriodicScheduleList = new ArrayList<>();
        for (Schedule schedule: this.scheduleList) {
            if (schedule instanceof  Periodic_Schedule)
                continue;
            Non_Periodic_Schedule nonPeriodicSchedule = (Non_Periodic_Schedule)schedule;
            if(nonPeriodicSchedule.getStartTime().isAfter(startTime)
                    && nonPeriodicSchedule.getEndTime().isBefore(endTime))
                nonPeriodicScheduleList.add(nonPeriodicSchedule);
        }

        return nonPeriodicScheduleList;
    }
}
