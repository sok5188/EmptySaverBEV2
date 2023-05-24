package com.example.emptySaver.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

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


    //@OneToOne(mappedBy = "timeTable")
    @ToString.Exclude
    @OneToOne(fetch = FetchType.LAZY )//, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "member_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Member member;

    @ToString.Exclude
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "team_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Team team;

    //외래키 저장을 상대에게 위임 -> 상대는 @joinColumn에 외래키 저장
    @Builder.Default
    @OneToMany(mappedBy = "timeTable", fetch = FetchType.LAZY,  cascade = CascadeType.REMOVE)//, cascade = CascadeType.ALL,orphanRemoval = true) //casecade all로 이 table사라지면 일정도 같이 제거됨
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

    public long[] calcPeriodicScheduleInBound(final LocalDateTime start, final LocalDateTime end){
        this.weekScheduleData = new long[]{0,0,0,0,0,0,0};   //0으로 init 후 재계산

        for (Periodic_Schedule schedule: this.getPeriodicScheduleInBound(start, end)) {
            addWeekScheduleData(schedule.getWeekScheduleData());
        }
        return this.weekScheduleData;
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

    public List<Periodic_Schedule> getPeriodicScheduleInBound(final LocalDateTime start, final LocalDateTime end){
        LocalDateTime startTime = start.minusMinutes(1);
        LocalDateTime endTime = end.plusMinutes(1);

        List<Periodic_Schedule> periodicScheduleList = new ArrayList<>();
        for (Schedule schedule: this.scheduleList) {
            if (schedule instanceof  Non_Periodic_Schedule)
                continue;
            Periodic_Schedule periodicSchedule = (Periodic_Schedule)schedule;
            if(periodicSchedule.getStartTime() == null){
                periodicScheduleList.add(periodicSchedule);
            }
            /*
            else if(periodicSchedule.getStartTime().isAfter(startTime)
                    && periodicSchedule.getEndTime().isBefore(endTime))
                periodicScheduleList.add(periodicSchedule);*/
        }

        return periodicScheduleList;
    }

    public List<Periodic_Schedule> getPeriodicScheduleOverlap(final LocalDateTime startTime, final LocalDateTime endTime){

        List<Periodic_Schedule> periodicScheduleList = new ArrayList<>();
        for (Schedule schedule: this.scheduleList) {
            if (schedule instanceof  Non_Periodic_Schedule)
                continue;
            if(schedule.getStartTime() == null)
                continue;
            Periodic_Schedule periodicSchedule = (Periodic_Schedule)schedule;
            if((periodicSchedule.getStartTime().isBefore(endTime) && periodicSchedule.getEndTime().isAfter(endTime.minusMinutes(1)))
                    ||(startTime.isBefore(periodicSchedule.getEndTime()) && periodicSchedule.getEndTime().isBefore(endTime.plusMinutes(1))))
                periodicScheduleList.add(periodicSchedule);
        }

        return periodicScheduleList;
    }
}
