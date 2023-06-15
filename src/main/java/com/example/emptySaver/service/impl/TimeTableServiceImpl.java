package com.example.emptySaver.service.impl;

import com.example.emptySaver.domain.dto.GroupDto;
import com.example.emptySaver.domain.dto.TimeTableDto;
import com.example.emptySaver.domain.entity.*;
import com.example.emptySaver.domain.entity.category.Category;
import com.example.emptySaver.errorHandler.BaseException;
import com.example.emptySaver.errorHandler.BaseResponseStatus;
import com.example.emptySaver.repository.*;
import com.example.emptySaver.service.*;
import com.example.emptySaver.utils.TimeDataSuperUltraConverter;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TimeTableServiceImpl implements TimeTableService {
    private final int TEAM_SCHEDULE_EXPIRATION_NUM = 30;

    @PersistenceContext
    private final EntityManager em;

    private final TimeDataSuperUltraConverter timeDataConverter;
    private final ScheduleRepository scheduleRepository;
    private final PeriodicScheduleRepository periodicScheduleRepository;
    private final NonPeriodicScheduleRepository nonPeriodicScheduleRepository;
    private final MemberRepository memberRepository;
    private final TeamRepository teamRepository;
    private final ScheduleMemberRepository scheduleMemberRepository;

    private final GroupService groupService;
    private final MemberService memberService;
    private final FCMService fcmService;
    private final CategoryService categoryService;
    private final FriendService friendService;

    private Map<String,Integer> dayToIntMap = Map.of("월",0, "화", 1,"수",2,"목",3,"금",4,"토",5,"일",6);

    public Schedule getScheduleById(final Long scheduleId) {
        Optional<Schedule> scheduleOptional = scheduleRepository.findById(scheduleId);

        if(scheduleOptional.isEmpty())  {//존재x 스케줄
            throw new BaseException(BaseResponseStatus.NOT_EXIST_SCHEDULE_ID);
        }

        return scheduleOptional.get();
    }

    @Override
    public List<TimeTableDto.SearchedScheduleDto> convertScheduleListToSearchedScheduleDtoList(List<Schedule> scheduleList){
        List<TimeTableDto.SearchedScheduleDto> retList = new ArrayList<>();

        for (Schedule schedule : scheduleList) {
            TimeTableDto.SearchedScheduleDto build = TimeTableDto.SearchedScheduleDto.builder()
                    .id(schedule.getId())
                    .name(schedule.getName())
                    .body(schedule.getBody())
                    .category(schedule.getCategory())
                    .subCategory(schedule.getSubCategory())
                    //.groupInfo(groupService.getGroupDetails(schedule.getTimeTable().getTeam().getId()))
                    .timeData(this.timeDataConverter.convertScheduleTimeDataToString(schedule))
                    .periodicType(false)
                    .build();
            if(schedule.getTimeTable() != null && schedule.getTimeTable().getTeam() != null){
                Team team = schedule.getTimeTable().getTeam();
                GroupDto.DetailGroupRes groupBuild = GroupDto.DetailGroupRes.builder()
                        .groupId(team.getId()).groupName(team.getName()).oneLineInfo(team.getOneLineInfo())
                        .groupDescription(team.getDescription())//.nowMember(Long.valueOf(memberTeamRepository.countByTeam(team)))
                        .maxMember(team.getMaxMember()).isPublic(team.isPublic())//.categoryLabel(categoryService.getLabelByCategory(team.getCategory()))
                        .build();

                build.setGroupInfo(groupBuild);
            }

            if (schedule instanceof Periodic_Schedule)
                build.setPeriodicType(true);

            retList.add(build);
        }

        return retList;
    }

    @Override
    public List<TimeTableDto.SearchedScheduleDto> getSearchedScheduleDtoList(final TimeTableDto.ScheduleSearchRequestForm searchForm){
        int dayOfWeek = searchForm.getEndTime().getDayOfWeek().getValue() -1;

        List<Schedule> includedScheduleList = new ArrayList<>();

        List<Periodic_Schedule> periodicScheduleList = periodicScheduleRepository.findByPublicType(true);
        for (Periodic_Schedule periodicSchedule : periodicScheduleList) {
            if(periodicSchedule.getStartTime() == null){
                if(timeDataConverter.checkBitsIsBelongToLocalDataTime(periodicSchedule.getWeekScheduleData()[dayOfWeek]
                        ,searchForm.getStartTime(), searchForm.getEndTime()))
                    includedScheduleList.add(periodicSchedule);
            }else if((periodicSchedule.getStartTime().isAfter(searchForm.getStartTime().minusMinutes(1)) && periodicSchedule.getStartTime().isBefore(searchForm.getEndTime().plusMinutes(1)))
                    || periodicSchedule.getEndTime().isAfter(searchForm.getStartTime().minusMinutes(1)) && periodicSchedule.getEndTime().isBefore(searchForm.getEndTime().plusMinutes(1))){
                if(timeDataConverter.checkBitsIsBelongToLocalDataTime(periodicSchedule.getWeekScheduleData()[dayOfWeek]
                        ,searchForm.getStartTime(), searchForm.getEndTime()))
                    includedScheduleList.add(periodicSchedule);
            }
        }

        List<Non_Periodic_Schedule> nonPeriodicScheduleList = nonPeriodicScheduleRepository.findByPublicTypeAndStartTimeBetween(true, searchForm.getStartTime(), searchForm.getEndTime());
        for (Non_Periodic_Schedule nonPeriodicSchedule : nonPeriodicScheduleList) {
            if(nonPeriodicSchedule.getEndTime().isBefore(searchForm.getEndTime().plusMinutes(1)))
                includedScheduleList.add(nonPeriodicSchedule);
        }

        return this.convertScheduleListToSearchedScheduleDtoList(includedScheduleList);
    }

    //그룹장에게 schedule 저장
    @Transactional
    private void saveScheduleInOwnerTimeTable(final TimeTableDto.SchedulePostDto schedulePostDto, final Long originScheduleId){
        Member member = memberService.getMember();
        Time_Table timeTable = member.getTimeTable();

        Schedule schedule = this.convertDtoToSchedule(schedulePostDto);
        schedule.setTimeTable(timeTable);
        schedule.setOriginScheduleId(originScheduleId);
        schedule.setPublicType(false);

        schedule.setTimeTable(timeTable);
        Schedule savedSchedule = scheduleRepository.save(schedule);//@JoinColumn을 가지고 있는게 주인이므로 set은 Schedule이

        List<Schedule> scheduleList = timeTable.getScheduleList();
        scheduleList.add(savedSchedule);
        //timeTable.calcAllWeekScheduleData();

        log.info("add Schedule"+ savedSchedule.getId() + " to Member" + member.getId());
    }

    @Override
    @Transactional
    public void saveScheduleByTeam(final Long teamId, final Long OwnerId,final boolean isPublicTypeSchedule, final TimeTableDto.SchedulePostDto schedulePostDto){
        em.flush();
        em.clear();

        log.info("Got groupID :"+teamId);
        Team team = teamRepository.findFirstWithCategoryById(teamId).orElseThrow(()-> new BaseException(BaseResponseStatus.INVALID_TEAM_ID));
        Time_Table teamTimeTable = team.getTimeTable();

        List<Long> memberIdList = new ArrayList<>();
        List<MemberTeam> teamMembers = team.getTeamMembers();
        for (MemberTeam teamMember : teamMembers) {     //그룹원들  id 긁어오기
            if(!OwnerId.equals(teamMember.getMember().getId())&&teamMember.isBelong())
                memberIdList.add(teamMember.getMember().getId());
        }

        schedulePostDto.setGroupId(teamId);
        schedulePostDto.setGroupName(team.getName());
        schedulePostDto.setGroupType(true);
        Schedule schedule = this.convertDtoToSchedule(schedulePostDto);
        schedule.setHideType(false);
        schedule.setTimeTable(teamTimeTable);
        schedule.setPublicType(isPublicTypeSchedule);

        Category teamCategory = team.getCategory();
        if(schedule.getCategory() == null || schedule.getCategory().isEmpty()){   //스케줄의 카테고리 설정이 없다면 그룹의 카테고리로 만듬.
            //log.info("cate : " + teamCategory.getClass());
            schedule.setCategory(categoryService.getCategoryNameByCategory(teamCategory));
            schedule.setSubCategory(categoryService.getLabelByCategory(teamCategory));
        }

        this.checkValidSchedule(teamTimeTable, schedule);
        Schedule savedSchedule = scheduleRepository.save(schedule);//@JoinColumn을 가지고 있는게 주인이므로 set은 Schedule이
        savedSchedule.setOriginScheduleId(savedSchedule.getId());   //원본id 저장
        //log.info("team savedSchedule: "+ savedSchedule.getGroupName());

        List<Schedule> scheduleList = teamTimeTable.getScheduleList();
        scheduleList.add(savedSchedule);
        //teamTimeTable.calcAllWeekScheduleData();

        //this.saveScheduleInOwnerTimeTable(schedulePostDto,savedSchedule.getId());    //그룹장 자신에게 저장
        this.setCheckTeamSchedule(savedSchedule.getId(),true, schedulePostDto.getHideType());


        //그룹원들에게 알림보내기
        //TODO : 그룹 스케줄이 몇시부터 몇시 무슨요일에 추가되는 지를 바디에 넣고 data부분에 스케쥴 아이디?..
        // 일단 body부분은 사용자가 보기 좋은 말로 보내는게 좋을 듯 함니다.
        fcmService.sendMessageToMemberList(memberIdList,"그룹 " +team.getName()+"의 일정 추가 요청","그룹 " +team.getName()+"에서 일정 추가 알림이 왔어요! 지금 바로 확인해 보세요!"
                ,"notification","Schedule",String.valueOf(savedSchedule.getId()),"group", String.valueOf(teamId));

        log.info("add Schedule id: "+ savedSchedule.getId() + " to Team id: " + team.getId());
    }

    private List<TimeTableDto.TeamScheduleDto> convertSchedulesToTeamScheduleDtoList(final List<Schedule> scheduleList){
        List<TimeTableDto.TeamScheduleDto> ret = new ArrayList<>();

        for (Schedule schedule: scheduleList) {
            boolean periodicType = true;
            if(schedule instanceof Non_Periodic_Schedule)
                periodicType = false;

            ret.add(TimeTableDto.TeamScheduleDto.builder()
                    .id(schedule.getId())
                    .body(schedule.getBody())
                    .periodicType(periodicType)
                    .name(schedule.getName())
                    .isRead(false)
                    .timeData(this.timeDataConverter.convertScheduleTimeDataToString(schedule))
                    .build());
        }

        return ret;
    }

    @Override
    public List<TimeTableDto.TeamScheduleDto> getTeamScheduleList(final Long teamId){
        Team team = teamRepository.findById(teamId).orElseThrow(()-> new BaseException(BaseResponseStatus.INVALID_TEAM_ID));
        Time_Table timeTable = team.getTimeTable();

        Member member = memberService.getMember();

        List<Schedule> scheduleList = timeTable.getScheduleList();
        log.info("scheduleList size: "+scheduleList.size());

        List<Schedule> currentScheduleList = new ArrayList<>();
        LocalDateTime expirationDate = LocalDateTime.now().minusDays(TEAM_SCHEDULE_EXPIRATION_NUM);

        for (Schedule schedule : scheduleList) {
            if (schedule instanceof Periodic_Schedule){
                if(schedule.getEndTime() != null && schedule.getEndTime().isBefore(expirationDate)) //만료기간 이전꺼는 반영 x
                        continue;
            }else{
                if(schedule.getEndTime().isBefore(expirationDate)) //만료기간 이전꺼는 반영 x
                    continue;
            }
            currentScheduleList.add(schedule);
        }

        List<TimeTableDto.TeamScheduleDto> teamScheduleDtoList = this.convertSchedulesToTeamScheduleDtoList(currentScheduleList);

        for (int i = 0; i < teamScheduleDtoList.size(); i++) {
            Optional<ScheduleMember> isRead = scheduleMemberRepository.findByMemberAndSchedule(member, currentScheduleList.get(i));

            if (!isRead.isEmpty())  {
                //log.info("read");
                teamScheduleDtoList.get(i).setRead(true);
            }
        }
        return teamScheduleDtoList;
    }

    @Override
    public void setCheckTeamSchedule(final Long scheduleId, final boolean accept, boolean hideType) {
        Member member = memberService.getMember();
        Schedule schedule = this.getScheduleById(scheduleId);
        ScheduleMember build = ScheduleMember.builder().member(member).schedule(schedule).accept(accept).build();
        ScheduleMember savedEntity = scheduleMemberRepository.save(build);

        if(accept){ //유저의 스케줄로도 저장
            this.saveScheduleInDB(member.getId(), scheduleId, hideType);
        }
        log.info("accpet?: " +savedEntity.isAccept());
    }

    @Override
    public TimeTableDto.MemberAllTimeTableInfo getMemberAllTimeTableInfo(Long memberId, LocalDate startDate, LocalDate endDate){
        TimeTableDto.TimeTableInfo memberTimeTable = this.getMemberTimeTableByDayNum(memberId, startDate, endDate, true);
        List<TimeTableDto.GroupTimeTableInfo> groupOfMemberTimeTableList = this.getGroupOfMemberTimeTableList(memberId, startDate, endDate);

        return TimeTableDto.MemberAllTimeTableInfo.builder()
                .memberTimeTable(memberTimeTable)
                .groupTimeTableList(groupOfMemberTimeTableList)
                .build();
    }

    private List<TimeTableDto.GroupTimeTableInfo> getGroupOfMemberTimeTableList(Long memberId,LocalDate startDate, LocalDate endDate){
        List<TimeTableDto.GroupTimeTableInfo> groupTimeTableInfoList = new ArrayList<>();

        Member member = memberRepository.findById(memberId).get();

        List<Team> groupList = new ArrayList<>();
        for (MemberTeam team : member.getMemberTeam()) {
            groupList.add(team.getTeam());
        }
        endDate = endDate.plusDays(1);
        LocalDateTime startTime = startDate.atStartOfDay();
        LocalDateTime endTime = endDate.atStartOfDay();
        for (Team team : groupList) {
            Time_Table teamTimeTable = team.getTimeTable();
            TimeTableDto.TimeTableInfo timeTableInfo = this.calcTimeTableDataPerWeek(startDate, endDate
                    , teamTimeTable.calcPeriodicScheduleInBound(true), teamTimeTable.getPeriodicScheduleInBound(true)
                    ,teamTimeTable.getPeriodicScheduleOverlap(startTime,endTime, true),teamTimeTable.getNonPeriodicScheduleInBound(startTime,endTime, true));
            groupTimeTableInfoList.add(TimeTableDto.GroupTimeTableInfo.builder()
                    .groupId(team.getId())
                    .timeTableInfo(timeTableInfo)
                    .build());
        }

        return groupTimeTableInfoList;
    }

    @Override
    public TimeTableDto.TimeTableInfo getMemberTimeTableByDayNum(Long memberId, LocalDate startDate, LocalDate endDate, boolean getHideType){
        Member member = memberRepository.findById(memberId).get();

        Time_Table timeTable = member.getTimeTable();
        //timeTable.calcAllWeekScheduleData();
        endDate = endDate.plusDays(1);
        long[] weekScheduleData = timeTable.calcPeriodicScheduleInBound(getHideType);
        //final List<Schedule> scheduleList = timeTable.getScheduleList();
        List<Periodic_Schedule> periodicScheduleInBound = timeTable.getPeriodicScheduleInBound(getHideType);
        List<Periodic_Schedule> periodicScheduleOverlap = timeTable.getPeriodicScheduleOverlap(startDate.atStartOfDay(), endDate.atStartOfDay(), getHideType);
        List<Non_Periodic_Schedule> nonPeriodicScheduleInBound = timeTable.getNonPeriodicScheduleInBound(startDate.atStartOfDay(), endDate.atStartOfDay(), getHideType);

        return calcTimeTableDataPerWeek(startDate,endDate,weekScheduleData,periodicScheduleInBound,periodicScheduleOverlap,nonPeriodicScheduleInBound);
    }

    private List<List<Boolean>> convertLongListToBitListsPerDay(List<Long> bitDataPerDays){
        List<List<Boolean>> bitListsPerDay = new ArrayList<>();

        for (Long bits: bitDataPerDays) {
            List<Boolean> bitList = timeDataConverter.convertLongToBooleanList(bits);
            bitListsPerDay.add(bitList);
        }

        return bitListsPerDay;
    }

    private TimeTableDto.TimeTableInfo calcTimeTableDataPerWeek( final LocalDate startDate, final LocalDate endDate ,final long[] weekScheduleData
            ,final List<Periodic_Schedule> periodicScheduleInBound, List<Periodic_Schedule> periodicScheduleOverlap,List<Non_Periodic_Schedule> nonPeriodicScheduleInBound){
        final int dayNum = (int) Duration.between(startDate.atStartOfDay(),endDate.atStartOfDay()).toDays(); //+1;
        final int startDayOfWeek = startDate.getDayOfWeek().getValue() -1; //월요일부터 0~6까지 정수

        List<Long> bitDataPerDays = new ArrayList<>();
        List<List<TimeTableDto.ScheduleDto>> scheduleListPerDays  = new ArrayList<List<TimeTableDto.ScheduleDto>>();

        int dayOfWeekIdx = startDayOfWeek;
        final int WEEK_MOD = 7;
        for (int i = 0; i < dayNum; i++) {  //주기 데이터 저장
            bitDataPerDays.add(weekScheduleData[dayOfWeekIdx]);
            ++dayOfWeekIdx;
            dayOfWeekIdx %= WEEK_MOD;
            scheduleListPerDays.add(new ArrayList<>());
        }
/*
        List<Periodic_Schedule> periodicScheduleList = new ArrayList<>();
        List<Non_Periodic_Schedule> nonPeriodicScheduleList = new ArrayList<>();
        for (Schedule schedule: scheduleList) { //타입 분리
            if(schedule instanceof Periodic_Schedule){
                periodicScheduleList.add((Periodic_Schedule)schedule);
            }
            else {
                Non_Periodic_Schedule nonPeriodicSchedule = (Non_Periodic_Schedule) schedule;
                if((nonPeriodicSchedule.getStartTime().isAfter(startDate.atStartOfDay())
                        && nonPeriodicSchedule.getEndTime().isBefore(endDate.plusDays(1).atStartOfDay())) )  //날짜 범위 내의 데이터만 저장
                    nonPeriodicScheduleList.add((Non_Periodic_Schedule)schedule);
            }

        }*/

        List<List<TimeTableDto.ScheduleDto>> weekRoutines = new ArrayList<>();
        for (int i = 0; i <WEEK_MOD ; i++)    //init
            weekRoutines.add(new ArrayList<>());

        for (Periodic_Schedule schedule:periodicScheduleInBound) { //weekRoutine인 스케줄 저장
            long[] weekBits = schedule.getWeekScheduleData();
            for(int day =0; day< WEEK_MOD ; ++day)
                if(weekBits[day] >0) {  //Dto Convert
                    log.info("return: schedule.getGroupId: "+schedule.getGroupId());
                    weekRoutines.get(day).add(
                            TimeTableDto.ScheduleDto.builder()
                                    .id(schedule.getId())
                                    .name(schedule.getName())
                                    .body(schedule.getBody())
                                    .groupType(schedule.isGroupType())
                                    .hideType(schedule.isHideType())
                                    .groupId(schedule.getGroupId())
                                    .groupName(schedule.getGroupName())
                                    .timeData(this.timeDataConverter.convertLongToBooleanList(weekBits[day]))
                                    .timeStringData(timeDataConverter.bitTimeDataToStringData(weekBits[day]))
                                    .build());
                }
        }

        dayOfWeekIdx = startDayOfWeek;
        for (int day = 0; day <dayNum ; day++) {
            scheduleListPerDays.get(day).addAll(weekRoutines.get(dayOfWeekIdx));
            ++dayOfWeekIdx;
            dayOfWeekIdx %= WEEK_MOD;
        }

        for (Periodic_Schedule schedule:periodicScheduleOverlap) { //overlap은 직접 하나하나
            long[] weekBits = schedule.getWeekScheduleData();
            dayOfWeekIdx = schedule.getStartTime().getDayOfWeek().getValue() -1;
            int startDay = (int) Duration.between(startDate.atStartOfDay(),schedule.getStartTime().toLocalDate().atStartOfDay()).toDays();
            if(startDay <0){
                startDay = 0;
                dayOfWeekIdx = startDayOfWeek;
            }
            int endDay = (int) Duration.between(startDate.atStartOfDay(),schedule.getEndTime().toLocalDate().atStartOfDay()).toDays() +1;
            if(endDay >dayNum)
                endDay = dayNum;
            for(int day =startDay; day< endDay ; ++day) {
                if (weekBits[dayOfWeekIdx] > 0) {  //Dto Convert
                    //log.info("return: schedule.getGroupId: "+schedule.getGroupId());
                    TimeTableDto.ScheduleDto build = TimeTableDto.ScheduleDto.builder()
                            .id(schedule.getId())
                            .name(schedule.getName())
                            .body(schedule.getBody())
                            .groupType(schedule.isGroupType())
                            .hideType(schedule.isHideType())
                            .groupId(schedule.getGroupId())
                            .groupName(schedule.getGroupName())
                            .timeData(this.timeDataConverter.convertLongToBooleanList(weekBits[dayOfWeekIdx]))
                            .timeStringData(timeDataConverter.bitTimeDataToStringData(weekBits[dayOfWeekIdx]))
                            .build();
                    scheduleListPerDays.get(day).add(build);
                }
                ++dayOfWeekIdx;
                dayOfWeekIdx %= WEEK_MOD;
            }
        }

        for (Non_Periodic_Schedule schedule: nonPeriodicScheduleInBound ) {
            LocalDateTime scheduleStartTime = schedule.getStartTime();
            Long timeBitData = timeDataConverter.convertTimeToBit(scheduleStartTime, schedule.getEndTime());
            LocalDate startLocalDate = LocalDate.of(scheduleStartTime.getYear(), scheduleStartTime.getMonth(), scheduleStartTime.getDayOfMonth());
            int afterDayNumFromStart = (int) Duration.between(startDate.atStartOfDay(),startLocalDate.atStartOfDay()).toDays();

            scheduleListPerDays.get(afterDayNumFromStart).add(
                    TimeTableDto.ScheduleDto.builder()
                            .id(schedule.getId())
                            .name(schedule.getName())
                            .body(schedule.getBody())
                            .groupType(schedule.isGroupType())
                            .hideType(schedule.isHideType())
                            .groupId(schedule.getGroupId())
                            .groupName(schedule.getGroupName())
                            .timeData(this.timeDataConverter.convertLongToBooleanList(timeBitData))
                            .timeStringData(timeDataConverter.bitTimeDataToStringData(timeBitData))
                            .build());

            Long targetBits = bitDataPerDays.get(afterDayNumFromStart);
            bitDataPerDays.set(afterDayNumFromStart,targetBits|timeBitData);
        }


        return TimeTableDto.TimeTableInfo.builder()
                .startDate(startDate)
                .endData(endDate.minusDays(1))
                .bitListsPerDay(this.convertLongListToBitListsPerDay(bitDataPerDays))
                .scheduleListPerDays(scheduleListPerDays).build();
    }

    private long[] convertTimeStringsToBitsArray(List<String> periodicTimeStringList){
        long[] bitsArray = {0,0,0,0,0,0,0};
        for (String time: periodicTimeStringList) {
            String[] splitData = time.split(",");
            Integer dayNumber = dayToIntMap.get(splitData[0]);
            String[] duration = splitData[1].split("-");

            int startIdx = (int) (this.timeDataConverter.timeStringToFloat(duration[0])*2);
            int endIdx = (int) (this.timeDataConverter.timeStringToFloat(duration[1])*2);

            long moveBit =(1l << startIdx);
            for (int i = startIdx; i <endIdx ; i++) {
                bitsArray[dayNumber] |= moveBit;
                moveBit <<=1;
            }
        }

        return bitsArray;
    }

    private Schedule convertDtoToSchedule(TimeTableDto.SchedulePostDto schedulePostData){
        if(schedulePostData.getPeriodicType().equals("true")){
            Periodic_Schedule periodicSchedule = new Periodic_Schedule();
            periodicSchedule.setWeekScheduleData(this.convertTimeStringsToBitsArray(schedulePostData.getPeriodicTimeStringList()));
            periodicSchedule.setName(schedulePostData.getName());
            periodicSchedule.setBody(schedulePostData.getBody());
            periodicSchedule.setCategory(schedulePostData.getCategory());
            periodicSchedule.setSubCategory(schedulePostData.getSubCategory());
            periodicSchedule.setStartTime(schedulePostData.getStartTime());
            periodicSchedule.setEndTime(schedulePostData.getEndTime());
            periodicSchedule.setGroupId(schedulePostData.getGroupId());
            periodicSchedule.setGroupType(schedulePostData.getGroupType());
            periodicSchedule.setGroupName(schedulePostData.getGroupName());

            periodicSchedule.setHideType(schedulePostData.getHideType());
            log.info("build Periodic Schedule " + periodicSchedule.toString());
            return periodicSchedule;
        }
        Non_Periodic_Schedule nonPeriodicSchedule = new Non_Periodic_Schedule();
        nonPeriodicSchedule.setName(schedulePostData.getName());
        nonPeriodicSchedule.setStartTime(schedulePostData.getStartTime());
        nonPeriodicSchedule.setEndTime(schedulePostData.getEndTime());
        nonPeriodicSchedule.setBody(schedulePostData.getBody());
        nonPeriodicSchedule.setCategory(schedulePostData.getCategory());
        nonPeriodicSchedule.setSubCategory(schedulePostData.getSubCategory());
        nonPeriodicSchedule.setGroupId(schedulePostData.getGroupId());
        nonPeriodicSchedule.setGroupType(schedulePostData.getGroupType());
        nonPeriodicSchedule.setGroupName(schedulePostData.getGroupName());

        nonPeriodicSchedule.setHideType(schedulePostData.getHideType());
        log.info("build NonPeriodic Schedule "+ nonPeriodicSchedule.toString());
        return  nonPeriodicSchedule;
    }

    @Override
    @Transactional
    public void saveScheduleInDB(final Long memberId, final Long scheduleId, boolean hideType){
        Member member = memberService.getMember();
        Time_Table timeTable = member.getTimeTable();

        Schedule schedule = this.getScheduleById(scheduleId);
        Schedule copySchedule;
        if(schedule instanceof Subject){
            copySchedule = Subject.copySchedule((Subject) schedule);
        }else if(schedule instanceof Periodic_Schedule) {
            copySchedule = Periodic_Schedule.copySchedule((Periodic_Schedule) schedule);
        }else{
            copySchedule = Non_Periodic_Schedule.copySchedule((Non_Periodic_Schedule) schedule);
        }

        copySchedule.setTimeTable(timeTable);
        copySchedule.setPublicType(false);
        copySchedule.setHideType(hideType);

        this.checkValidSchedule(timeTable, copySchedule);
        //copySchedule.setTimeTable(timeTable);
        Schedule savedSchedule = scheduleRepository.save(copySchedule);//@JoinColumn을 가지고 있는게 주인이므로 set은 Schedule이

        log.info("savedSchedule: " + savedSchedule.getName() + "l " + savedSchedule.getStartTime());
        List<Schedule> scheduleList = timeTable.getScheduleList();
        scheduleList.add(savedSchedule);

        log.info("add Schedule"+ savedSchedule.getId() + " to Member" + member.getId());
    }

    public void checkValidSchedule(final Time_Table memberTimeTable, final Schedule schedule){
        if(schedule instanceof  Periodic_Schedule){
            if(!memberTimeTable.isPeriodicNotOverlapWithExistSchedule((Periodic_Schedule) schedule))
                throw new BaseException(BaseResponseStatus.TIME_IS_OVERLAP);
        }else{
            Non_Periodic_Schedule nonPeriodicSchedule = (Non_Periodic_Schedule) schedule;
            if(!memberTimeTable.isTimeNotOverlapWithExistSchedule(nonPeriodicSchedule.getStartTime(), nonPeriodicSchedule.getEndTime()))
                throw new BaseException(BaseResponseStatus.TIME_IS_OVERLAP);
        }
    }


    //멤버로 스케줄 저장
    @Override
    @Transactional
    public void saveScheduleInTimeTable(final Long memberId, final TimeTableDto.SchedulePostDto schedulePostData){
        Member member = memberRepository.findById(memberId).get();
        Schedule schedule = this.convertDtoToSchedule(schedulePostData);

        Time_Table timeTable = member.getTimeTable();
        schedule.setTimeTable(timeTable);
        schedule.setPublicType(false);

        this.checkValidSchedule(timeTable, schedule);
        Schedule savedSchedule = scheduleRepository.save(schedule);//@JoinColumn을 가지고 있는게 주인이므로 set은 Schedule이
        log.info("savedSchedule: "+ savedSchedule.getGroupId());
        List<Schedule> scheduleList = timeTable.getScheduleList();
        scheduleList.add(savedSchedule);
        //timeTable.calcAllWeekScheduleData();

        log.info("add Schedule"+ savedSchedule.getId() + " to Member" + member.getId());
    }
    //멤버로 수정
    @Override
    @Transactional
    public void updateScheduleInTimeTable(final Long scheduleId, final Long memberId,TimeTableDto.SchedulePostDto updatePostData){
        this.deleteScheduleInTimeTable(scheduleId);
        this.saveScheduleInTimeTable(memberId, updatePostData);
        //Schedule schedule = this.getScheduleById(scheduleId);
//
//        if(schedule instanceof Periodic_Schedule)
//            updatePostData.setPeriodicType("true");
//
//        Schedule updateData = this.convertDtoToSchedule(updatePostData);
//
//        if(schedule instanceof Periodic_Schedule)
//            updatePeriodicSchedule((Periodic_Schedule)schedule, (Periodic_Schedule)updateData);
//        else
//            updateNonPeriodicSchedule((Non_Periodic_Schedule)schedule, (Non_Periodic_Schedule)updateData);
//
//        scheduleRepository.save(schedule);
        //savedSchedule.getTimeTable().calcAllWeekScheduleData();
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

    @Override
    @Transactional
    public void updateTeamSchedule(final Long teamId, final Long teamOwnerId,final Long scheduleId, TimeTableDto.SchedulePostDto updatePostData){
        this.deleteScheduleInTimeTable(scheduleId);

        boolean isPeriodicType = false;
        if(updatePostData.getPeriodicType().equals("true"))
            isPeriodicType = true;
        this.saveScheduleByTeam(teamId, teamOwnerId , isPeriodicType,updatePostData);
//        Team team = groupService.getTeamById(teamId);
//        Schedule schedule = this.getScheduleById(scheduleId);
//        Long originScheduleId = schedule.getId();
//        List<Schedule> copyScheduleList = scheduleRepository.findByOriginScheduleId(originScheduleId);
//        for (Schedule copySchedule : copyScheduleList) {
//            this.updateScheduleInTimeTable(copySchedule.getId(),updatePostData);
//        }
//
//        this.updateScheduleInTimeTable(originScheduleId,updatePostData);
        log.info("update team Schedule ");
    }

    @Override
    @Transactional
    public void deleteScheduleInTimeTable(final Long scheduleId){
        em.flush();
        em.clear();

        Schedule schedule = this.getScheduleById(scheduleId);

        Time_Table timeTable = schedule.getTimeTable();
        timeTable.getScheduleList().remove(schedule);   //서로의 연관관계 끊기
        //timeTable.calcAllWeekScheduleData();

        schedule.setTimeTable(null);                    //서로의 연관관계 끊기

        scheduleRepository.deleteById(scheduleId);
        log.info("delete Schedule "+ scheduleId);
    }

    @Override
    @Transactional
    public void deleteTeamSchedule(final Long teamId,final Long scheduleId){
        Team team = groupService.getTeamById(teamId);
        Schedule schedule = this.getScheduleById(scheduleId);
        Long originScheduleId = schedule.getId();
        List<Schedule> copyScheduleList = scheduleRepository.findByOriginScheduleId(originScheduleId);
        for (Schedule copySchedule : copyScheduleList) {
            this.deleteScheduleInTimeTable(copySchedule.getId());
        }

        log.info("delete team Schedule "+ scheduleId);
    }


}