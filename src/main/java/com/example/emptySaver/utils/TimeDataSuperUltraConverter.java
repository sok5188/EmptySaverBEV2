package com.example.emptySaver.utils;

import com.example.emptySaver.domain.entity.Non_Periodic_Schedule;
import com.example.emptySaver.domain.entity.Periodic_Schedule;
import com.example.emptySaver.domain.entity.Schedule;
import com.example.emptySaver.service.TimeTableService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class TimeDataSuperUltraConverter {
    private static final String EMPTY = "empty";

    private Map<Integer,String> intToDayMap = Map.of(0,"월",1, "화", 2,"수",3,"목",4,"금",5,"토",6,"일");

    public boolean checkBitsIsBelongToLocalDataTime(long targetBits, LocalDateTime startTime, LocalDateTime endTime){
        Long timeBits = this.convertTimeToBit(startTime, endTime);

        long andOpRes = timeBits & targetBits;

        if(andOpRes != targetBits)  //시간에 속한다면 targetBits가 그대로 남아야함
            return false;
        return true;
    }

    public Long convertTimeToBit(LocalDateTime startTime, LocalDateTime endTime){
        Long retBits = 0l;

        int startIdx = startTime.getHour()*2 + (startTime.getMinute()/30);
        int endIdx = endTime.getHour()*2 + (endTime.getMinute()/30);
        Long bits = 1l;
        bits <<= (startIdx);
        for(int i =0; i<(endIdx - startIdx) ; ++i){
            retBits |= bits;
            bits <<=1;
        }

        return retBits;
    }

    private String idxToTimeString(int idx){
        StringBuilder stringBuilder = new StringBuilder();
        int div = idx / 2;

        if(div<10)
            stringBuilder.append(0);
        stringBuilder.append(div);

        if (idx % 2>0)
            stringBuilder.append(":30");
        else
            stringBuilder.append(":00");

        return stringBuilder.toString();
    }

    public String bitTimeDataToStringData(long bitTimeData){

        StringBuilder stringBuilder = new StringBuilder();
        long moveBit = 1;

        List<Integer> timeIdxList = new ArrayList<>();
        for (int idx = 0; idx < 48; idx++) {
            if((bitTimeData & moveBit) >0){
                timeIdxList.add(idx);
            }
            moveBit <<=1;
        }

        if (timeIdxList.isEmpty())
            return stringBuilder.append(EMPTY).toString();

        int start = timeIdxList.get(0), end = timeIdxList.get(0) +1;
        for (int i = 0; i < timeIdxList.size()-1; i++) {
            if(timeIdxList.get(i) +1 == timeIdxList.get(i+1)){
                end = timeIdxList.get(i+1) +1;
            }else{
                stringBuilder.append(this.idxToTimeString(start) +"-" + this.idxToTimeString(end) +", ");
                start = end=timeIdxList.get(i+1);
                end++;
            }
        }
        stringBuilder.append(this.idxToTimeString(start) +"-" + this.idxToTimeString(end) );

        return stringBuilder.toString();
    }


    public String bitTimeDataArrayToStringData(long[] bitTimeData){
        StringBuilder stringBuilder = new StringBuilder();

        for (int day = 0; day < bitTimeData.length; day++) {
            String convertedDayTime = this.bitTimeDataToStringData(bitTimeData[day]);

            if(convertedDayTime.equals(EMPTY))  //빈 요일이면
                continue;

            stringBuilder.append(intToDayMap.get(day) +"/"+ convertedDayTime +",");
        }
        String builtString = stringBuilder.toString();
        return builtString.substring(0,builtString.length()-1);
    }

    public List<Boolean> convertLongToBooleanList(Long bits){
        List<Boolean> bitList = new ArrayList<>();
        long moveBit = 1l;

        for (int i = 0; i < 48 ; i++) {
            long andOpResult = bits & moveBit;
            boolean result = false;

            if(andOpResult >0l)
                result = true;

            bitList.add(result);
            moveBit <<= 1;
        }

        return bitList;
    }

    public String convertScheduleTimeDataToString(final Schedule schedule){
        StringBuilder stringBuilder = new StringBuilder();
        if (schedule instanceof Periodic_Schedule) {
            Periodic_Schedule periodicSchedule = (Periodic_Schedule) schedule;
            String ret = bitTimeDataArrayToStringData(periodicSchedule.getWeekScheduleData());
            stringBuilder.append(ret);
        }else{
            Non_Periodic_Schedule nonPeriodicSchedule = (Non_Periodic_Schedule) schedule;
            stringBuilder.append(nonPeriodicSchedule.getStartTime().toString());
            stringBuilder.append(" ~ ");
            stringBuilder.append(nonPeriodicSchedule.getEndTime().toString());
        }

        return stringBuilder.toString().replace("T"," ");
    }
}
