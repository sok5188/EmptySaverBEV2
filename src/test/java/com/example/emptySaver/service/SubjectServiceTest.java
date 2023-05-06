package com.example.emptySaver.service;

import com.example.emptySaver.domain.dto.SubjectDto;
import com.example.emptySaver.domain.dto.TimeTableDto;
import com.example.emptySaver.domain.entity.Member;
import com.example.emptySaver.domain.entity.Subject;
import com.example.emptySaver.domain.entity.Time_Table;
import com.example.emptySaver.repository.MemberRepository;
import com.example.emptySaver.repository.SubjectRepository;
import com.example.emptySaver.repository.TimeTableRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class SubjectServiceTest {
    @Autowired
    private SubjectService subjectService;
    @Autowired
    private SubjectRepository subjectRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private TimeTableRepository timeTableRepository;
    @Autowired
    private TimeTableService timeTableService;

    @Test
    void 강의를_주기_데이터로_저장(){
        Time_Table timeTable = Time_Table.builder().title("육사시미").weekScheduleData(new long[]{0l,0l,0l,0l,0l,0l,0l}).build();
        Time_Table savedTable = timeTableRepository.save(timeTable);

        Member member = Member.init().name("멤버").build();
        member.setTimeTable(savedTable);
        Member savedMember = memberRepository.save(member);

        long[] weekData = {0,100,100,100,0,0,0};
        Subject subject = new Subject();
        subject.setSubjectname("캡스톤");
        subject.setWeekScheduleData(weekData);
        Subject savedSubject = subjectRepository.save(subject);

        subjectService.saveSubjectToMemberSchedule(savedMember.getId(),savedSubject.getId());

        LocalDateTime startDate = LocalDateTime.of(2023, 4, 30,10,0);
        LocalDateTime endDate = LocalDateTime.of(2023, 5, 10,12,30);
        TimeTableDto.TimeTableInfo timeTableDto = timeTableService.getMemberTimeTableByDayNum(savedMember.getId(), startDate.toLocalDate(), endDate.toLocalDate());

        List<List<TimeTableDto.ScheduleDto>> scheduleListPerDays = timeTableDto.getScheduleListPerDays();
        for (List<TimeTableDto.ScheduleDto> ss: scheduleListPerDays) {
            System.out.println("======");
            for (TimeTableDto.ScheduleDto scheduleDto: ss) {
                System.out.println(scheduleDto);
                System.out.println("bits: "+ Long.toBinaryString(scheduleDto.getTimeBitData()));
            }
        }
    }

    @Test
    void 강의_검색_테스스(){
        Subject subject = new Subject();
        subject.setSubjectname("캡스톤");
        subjectRepository.save(subject);

        List<SubjectDto.SubjectInfo> searchedList = subjectService.getSubjectsMatchedName("캡");
        assertThat(searchedList.size()).isEqualTo(1);
        assertThat(searchedList.get(0).getSubjectname()).isEqualTo(subject.getSubjectname());
    }
}