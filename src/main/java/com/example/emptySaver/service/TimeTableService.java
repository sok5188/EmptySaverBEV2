package com.example.emptySaver.service;

import com.example.emptySaver.domain.entity.*;
import com.example.emptySaver.repository.ScheduleRepository;
import com.example.emptySaver.repository.TimeTableRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceContexts;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class TimeTableService {
    @PersistenceContext
    private final EntityManager em;

    private final TimeTableRepository timeTableRepository;
    private final ScheduleRepository scheduleRepository;
    private final MemberService memberService;

    //멤버로 스케줄 저장
    @Transactional
    public void saveScheduleInTimeTable(Schedule schedule, Member member){
        Time_Table timeTable = member.getTimeTable();
        schedule.setTimeTable(timeTable);
        Schedule savedSchedule = scheduleRepository.save(schedule);//@JoinColumn을 가지고 있는게 주인이므로 set은 Schedule이

        List<Schedule> scheduleList = timeTable.getScheduleList();
        scheduleList.add(savedSchedule);
        timeTable.calcAllWeekScheduleData();

        log.info("add Schedule"+ savedSchedule.getId() + " to Member" + member.getId());
    }

    //멤버로 수정
    @Transactional
    public void updateScheduleInTimeTable(Long scheduleId, Schedule updateData){
        Optional<Schedule> scheduleOptional = scheduleRepository.findById(scheduleId);
        if(scheduleOptional.isEmpty()){
            log.info("NoSuchScheduleId: " + scheduleId);
            return;
        }

        Schedule schedule = scheduleOptional.get();
        if(schedule instanceof Periodic_Schedule)
            updatePeriodicSchedule((Periodic_Schedule)schedule, (Periodic_Schedule)updateData);
        else
            updateNonPeriodicSchedule((Non_Periodic_Schedule)schedule, (Non_Periodic_Schedule)updateData);

        scheduleRepository.save(schedule);
    }

    @Transactional
    private void updatePeriodicSchedule(Periodic_Schedule schedule, Periodic_Schedule updateData){
        if(updateData.getWeekScheduleData() != null)
            schedule.setWeekScheduleData(updateData.getWeekScheduleData());
        if(updateData.getName() != null)
            schedule.setName(updateData.getName());
        //일단 확인용으로 두개만
        log.info("update Schedule "+ schedule.getId() + " as " + updateData.toString());
    }

    @Transactional
    private void updateNonPeriodicSchedule(Non_Periodic_Schedule schedule, Non_Periodic_Schedule updateData){
        if(updateData.getName() != null)
            schedule.setName(updateData.getName());
        if(updateData.getStartTime() != null)
            schedule.setStartTime(updateData.getStartTime());
        if(updateData.getEndTime() != null)
            schedule.setEndTime(updateData.getEndTime());
        //일단 확인용
        log.info("update Schedule "+ schedule.getId() + " as " + updateData.toString());
    }

    @Transactional
    public void deleteScheduleInTimeTable(Long scheduleId){
        em.flush();
        em.clear();
        Schedule schedule = scheduleRepository.findById(scheduleId).get();

        Time_Table timeTable = schedule.getTimeTable();
        timeTable.getScheduleList().remove(schedule);   //서로의 연관관계 끊기
        timeTable.calcAllWeekScheduleData();

        schedule.setTimeTable(null);                    //서로의 연관관계 끊기

        scheduleRepository.deleteById(scheduleId);
        log.info("delete Schedule "+ scheduleId);
    }

}
