package com.example.emptySaver.domain.entity;

import com.example.emptySaver.utils.TimeDataSuperUltraConverter;
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

    public long[] calcPeriodicScheduleInBound(boolean getHideType){
        this.weekScheduleData = new long[]{0,0,0,0,0,0,0};   //0으로 init 후 재계산

        for (Periodic_Schedule schedule: this.getPeriodicScheduleInBound(getHideType)) {
            addWeekScheduleData(schedule.getWeekScheduleData());
        }
        return this.weekScheduleData;
    }

    private void addWeekScheduleData(long[] otherScheduleData){
        for (int i=0; i<otherScheduleData.length ; ++i)
            this.weekScheduleData[i] |= otherScheduleData[i];
    }

    public List<Non_Periodic_Schedule> getNonPeriodicScheduleInBound(final LocalDateTime start, final LocalDateTime end, boolean getHideType){
        LocalDateTime startTime = start.minusMinutes(1);
        LocalDateTime endTime = end.plusMinutes(1);

        List<Non_Periodic_Schedule> nonPeriodicScheduleList = new ArrayList<>();
        for (Schedule schedule: this.scheduleList) {
            if(!getHideType && schedule.isHideType())
                continue;

            if (schedule instanceof  Periodic_Schedule)
                continue;
            Non_Periodic_Schedule nonPeriodicSchedule = (Non_Periodic_Schedule)schedule;
            if(nonPeriodicSchedule.getStartTime().isAfter(startTime)
                    && nonPeriodicSchedule.getEndTime().isBefore(endTime))
                nonPeriodicScheduleList.add(nonPeriodicSchedule);
        }

        return nonPeriodicScheduleList;
    }

    //무기한 주기 데이터만
    public List<Periodic_Schedule> getPeriodicScheduleInBound(boolean getHideType){
        List<Periodic_Schedule> periodicScheduleList = new ArrayList<>();
        for (Schedule schedule: this.scheduleList) {
            if(!getHideType && schedule.isHideType())
                continue;

            if (schedule instanceof  Non_Periodic_Schedule)
                continue;
            Periodic_Schedule periodicSchedule = (Periodic_Schedule)schedule;

            if(periodicSchedule.getStartTime() == null){    //무기한 주기 데이터만
                periodicScheduleList.add(periodicSchedule);
            }

        }

        return periodicScheduleList;
    }

    public List<Periodic_Schedule> getPeriodicScheduleOverlap(final LocalDateTime startTime, final LocalDateTime endTime, boolean getHideType){

        List<Periodic_Schedule> periodicScheduleList = new ArrayList<>();
        for (Schedule schedule: this.scheduleList) {
            if(!getHideType && schedule.isHideType())
                continue;

            if (schedule instanceof  Non_Periodic_Schedule)
                continue;
            if(schedule.getStartTime() == null)
                continue;
            Periodic_Schedule periodicSchedule = (Periodic_Schedule)schedule;
            if(this.isOverlapTimeWithPeriodicSchedule(periodicSchedule,startTime,endTime))
                periodicScheduleList.add(periodicSchedule);
        }

        return periodicScheduleList;
    }

    private boolean isOverlapTimeWithPeriodicSchedule(final Periodic_Schedule periodicSchedule, final LocalDateTime startTime, final LocalDateTime endTime){
        if((periodicSchedule.getStartTime().isBefore(endTime) && periodicSchedule.getEndTime().isAfter(endTime.minusMinutes(1)))
                ||(startTime.isBefore(periodicSchedule.getEndTime()) && periodicSchedule.getEndTime().isBefore(endTime.plusMinutes(1))))
            return true;
        return false;
    }

    private boolean isOverlapTime(final LocalDateTime a_startTime, final LocalDateTime a_endTime, final LocalDateTime b_startTime, final LocalDateTime b_endTime){
        if((a_startTime.isBefore(b_endTime) && a_endTime.isAfter(b_endTime.minusMinutes(1)))
                ||(b_startTime.isBefore(a_endTime) &&a_endTime.isBefore(b_endTime.plusMinutes(1))))
            return true;
        return false;
    }

    public final boolean isTimeNotOverlapWithExistSchedule(final LocalDateTime startTime, final LocalDateTime endTime){
        final long[] weekBits = this.calcPeriodicScheduleInBound(true);

        if(this.isBitsArrOverlapTime(weekBits,startTime,endTime)){
            System.out.println("찾았음1");
            return false;
        }

        for (Schedule schedule: this.scheduleList) {
            if (schedule instanceof  Periodic_Schedule) {
                Periodic_Schedule targetSchedule = (Periodic_Schedule) schedule;

                if (targetSchedule.getStartTime() == null) {  //무기한인 경우
                    continue;

                }else{
                    System.out.println("찾았음2");
                    if(this.isOverlapTimeWithPeriodicSchedule(targetSchedule,startTime,endTime)
                            && this.isBitsArrOverlapTime(targetSchedule.getWeekScheduleData(),startTime,endTime))
                        return false;
                }
            }else{  //비주기와 함꼐라면 두렵지 않어~~
                Non_Periodic_Schedule targetSchedule = (Non_Periodic_Schedule)schedule;
                if(this.isOverlapTime(targetSchedule.getStartTime(), targetSchedule.getEndTime(), startTime, endTime))
                    return false;
            }
        }
        return true;
    }

    public final boolean isPeriodicNotOverlapWithExistSchedule(Periodic_Schedule periodicSchedule){
        final long[] weekBits = this.calcPeriodicScheduleInBound(true);

        if(this.isOverlapBits(weekBits, periodicSchedule.getWeekScheduleData()))
            return false;

        for (Schedule schedule: this.scheduleList) {

            if (schedule instanceof  Periodic_Schedule){
                Periodic_Schedule targetSchedule = (Periodic_Schedule)schedule;

                if(targetSchedule.getStartTime() == null){  //무기한인 경우 위에서 확인
                    continue;

                }else if(periodicSchedule.getEndTime() == null){ //뮤기한 주기 데이터 저장 시도시
                    if(targetSchedule.getStartTime().isBefore(LocalDateTime.now().plusMinutes(1))
                            && targetSchedule.getEndTime().isAfter(LocalDateTime.now().minusMinutes(1)))
                        if(this.isOverlapBits(weekBits, targetSchedule.getWeekScheduleData()))
                            return false;
                }else{     //기한이 있는 주기 데이터 저장 시도 시

                    if(this.isOverlapTimeWithPeriodicSchedule(targetSchedule,periodicSchedule.getStartTime(), periodicSchedule.getEndTime())){
                        if(this.isOverlapBits(weekBits, targetSchedule.getWeekScheduleData()))
                            return false;
                    }

                }

            }else{  //비주기인 경우
                Non_Periodic_Schedule targetSchedule = (Non_Periodic_Schedule)schedule;

                if(periodicSchedule.getStartTime() == null){
                    if(this.isBitsArrOverlapTime(periodicSchedule.getWeekScheduleData(),targetSchedule.getStartTime(),targetSchedule.getEndTime())){
                        return false;
                    }
                }else{  //기한있는 주기 스케줄인 경우
                    if(this.isOverlapTimeWithPeriodicSchedule(periodicSchedule,targetSchedule.getStartTime(), targetSchedule.getEndTime())){
                        if(this.isBitsArrOverlapTime(periodicSchedule.getWeekScheduleData(),targetSchedule.getStartTime(),targetSchedule.getEndTime())){
                            return false;
                        }
                    }
                }

            }

        }

        return true;
    }

    //TODO: 하루이상의 비주기라면 바꿔야됨. startDayOfWeek를
    private final boolean isBitsArrOverlapTime(final long[] bitArr, final LocalDateTime startTime, final LocalDateTime endTime){
        final int startDayOfWeek = startTime.toLocalDate().getDayOfWeek().getValue() -1; //월요일부터 0~6까지 정수

        if(TimeDataSuperUltraConverter.checkBitsIsOverlapToLocalDataTime(bitArr[startDayOfWeek], startTime,endTime)){
            return true;
        }

        return false;
    }

    //겹치는 bit가 있는지 확인
    private final boolean isOverlapBits(final long[] a, final long[] b){
        for (int i = 0; i < a.length; i++) {
            if((a[i] & b[i]) > 0){
                return true;
            }
        }
        return false;
    }
}
