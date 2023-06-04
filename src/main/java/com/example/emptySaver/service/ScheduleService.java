package com.example.emptySaver.service;

import com.example.emptySaver.domain.dto.TimeTableDto;
import com.example.emptySaver.domain.entity.Non_Periodic_Schedule;
import com.example.emptySaver.domain.entity.Schedule;
import com.example.emptySaver.domain.entity.category.MovieType;
import com.example.emptySaver.repository.NonPeriodicScheduleRepository;
import com.example.emptySaver.repository.NonSubjectRepository;
import com.example.emptySaver.repository.ScheduleRepository;
import com.example.emptySaver.utils.TimeDataSuperUltraConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleService {
    private final ScheduleRepository scheduleRepository;
    private final NonPeriodicScheduleRepository nonPeriodicScheduleRepository;
    private final TimeDataSuperUltraConverter timeDataConverter;
    private final static String MOVIE = "movie";
    private final static String BY_API = "byAPI";

    public List<TimeTableDto.TeamScheduleDto> getMovieScheduleToDay(){
        LocalDateTime localDateTime = LocalDate.now().atStartOfDay();
        List<Non_Periodic_Schedule> byCategoryAndStartTimeBetween = nonPeriodicScheduleRepository.findByCategoryAndStartTimeBetween(MOVIE, localDateTime, localDateTime.plusDays(1));

        return this.convertSchedulesToTeamScheduleDtoList(byCategoryAndStartTimeBetween);
    }

    private List<TimeTableDto.TeamScheduleDto> convertSchedulesToTeamScheduleDtoList(final List<Non_Periodic_Schedule> scheduleList){
        List<TimeTableDto.TeamScheduleDto> ret = new ArrayList<>();

        for (Schedule schedule: scheduleList) {

            ret.add(TimeTableDto.TeamScheduleDto.builder()
                    .id(schedule.getId())
                    .body(schedule.getBody())
                    .periodicType(false)
                    .name(schedule.getName())
                    .timeData(this.timeDataConverter.convertScheduleTimeDataToString(schedule))
                    .build());
        }

        return ret;
    }

    @Transactional
    public void deleteAllSavedMovieBefore(){
        List<Non_Periodic_Schedule> movieList = nonPeriodicScheduleRepository.findBySubCategory(BY_API);
        if(!movieList.isEmpty()){ //전날 영화들이 존재하면 지워버림
            nonPeriodicScheduleRepository.deleteAll(movieList);

            log.info("delete all past movies");
            return;
        }

        log.info("no movie to delete");
    }

    @Transactional
    public void saveMovieScheduleList(final List<CrawlService.TmpMovie> movieList){
        for (CrawlService.TmpMovie movie : movieList) {
            for (CrawlService.RoomInfo roomInfo: movie.roomInfoList) {
                for (CrawlService.MovieTimeInfo timeInfo: roomInfo.timeInfoList) {
                    Non_Periodic_Schedule nonPeriodicSchedule = new Non_Periodic_Schedule();
                    nonPeriodicSchedule.setName(movie.title);
                    nonPeriodicSchedule.setPublicType(false);
                    nonPeriodicSchedule.setBody(timeInfo.reservationUrl);
                    nonPeriodicSchedule.setCategory(MOVIE);
                    nonPeriodicSchedule.setSubCategory(BY_API);
                    nonPeriodicSchedule.setPublicType(false);

                    String[] split = timeInfo.time.split(":");
                    int startHour = Integer.parseInt(split[0]);
                    if(startHour>=24)
                        continue;

                    int startMin = Integer.parseInt(split[1]);

                    LocalDateTime startTime = LocalDateTime.of(LocalDate.now(),LocalTime.of(startHour, startMin));
                    LocalDateTime endTime = startTime.plusMinutes(movie.runningTime);
                    if(endTime.toLocalDate().isAfter(endTime.toLocalDate()))    //다음 날까지 가는 영화는 일단 배제
                        continue;

                    nonPeriodicSchedule.setStartTime(startTime);
                    nonPeriodicSchedule.setEndTime(endTime);
                    Non_Periodic_Schedule save = scheduleRepository.save(nonPeriodicSchedule);
                    log.info("saved: " + save.getName() +", "+save.getEndTime());
                }

            }

        }
    }
}
