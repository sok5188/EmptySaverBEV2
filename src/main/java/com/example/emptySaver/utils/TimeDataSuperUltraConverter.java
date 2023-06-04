package com.example.emptySaver.utils;

import com.example.emptySaver.domain.entity.Non_Periodic_Schedule;
import com.example.emptySaver.domain.entity.Periodic_Schedule;
import com.example.emptySaver.domain.entity.Schedule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class TimeDataSuperUltraConverter {
    private static final String EMPTY = "empty";

    private Map<Integer,String> intToDayMap = Map.of(0,"월",1, "화", 2,"수",3,"목",4,"금",5,"토",6,"일");

    public static boolean checkBitsIsOverlapToLocalDataTime(long targetBits, LocalDateTime startTime, LocalDateTime endTime){
        Long timeBits = convertTimeToBit(startTime, endTime);

        if((targetBits & timeBits) == 0) {
            return false;
        }

        return true;
    }

    public static boolean checkBitsIsBelongToLocalDataTime(long targetBits, LocalDateTime startTime, LocalDateTime endTime){
        Long timeBits = convertTimeToBit(startTime, endTime);

        long andOpRes = targetBits & timeBits;

        if(andOpRes != targetBits) { //시간에 속한다면 targetBits가 그대로 남아야함
            return false;
        }

        return true;
    }

    public static Long convertTimeToBit(LocalDateTime startTime, LocalDateTime endTime){
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

    private String bitIdxToTimeString(int idx){
        StringBuilder stringBuilder = new StringBuilder();
        int div = idx / 2;

        if(div<10)
            stringBuilder.append(0);
        stringBuilder.append(div);

        if (idx % 2>0)
            stringBuilder.append("시 30분");
        else
            stringBuilder.append("시");

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
                stringBuilder.append(this.bitIdxToTimeString(start) +"-" + this.bitIdxToTimeString(end) +", ");
                start = end=timeIdxList.get(i+1);
                end++;
            }
        }
        stringBuilder.append(this.bitIdxToTimeString(start) +"-" + this.bitIdxToTimeString(end) );

        return stringBuilder.toString();
    }


    public String bitTimeDataArrayToStringData(long[] bitTimeData){
        StringBuilder stringBuilder = new StringBuilder();

        for (int day = 0; day < bitTimeData.length; day++) {
            String convertedDayTime = this.bitTimeDataToStringData(bitTimeData[day]);

            if(convertedDayTime.equals(EMPTY))  //빈 요일이면
                continue;

            stringBuilder.append(intToDayMap.get(day) +":"+ convertedDayTime +", ");
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
            if(periodicSchedule.getStartTime() == null)
                stringBuilder.append("무기한/ ");
            else{
                stringBuilder.append("기한: ");
                stringBuilder.append(this.localDateTimeToPretty(periodicSchedule.getStartTime()));
                stringBuilder.append(" ~ ");
                stringBuilder.append(this.localDateTimeToPretty(periodicSchedule.getEndTime()) + " / ");
            }
            String ret = bitTimeDataArrayToStringData(periodicSchedule.getWeekScheduleData());
            stringBuilder.append(ret);
        }else{
            Non_Periodic_Schedule nonPeriodicSchedule = (Non_Periodic_Schedule) schedule;
            LocalDateTime startTime = nonPeriodicSchedule.getStartTime();
            LocalDateTime endTime = nonPeriodicSchedule.getEndTime();

            stringBuilder.append(this.localDateTimeToPretty(startTime));
            stringBuilder.append(" ~ ");

            if(startTime.toLocalDate().equals(endTime.toLocalDate()))
                stringBuilder.append(this.localDateTimeToPrettyOnlyTime(endTime));
            else
                stringBuilder.append(this.localDateTimeToPretty(endTime));
        }

        return stringBuilder.toString();
    }

    private String localDateTimeToPretty(final LocalDateTime dataTime){
        StringBuilder stringBuilder = new StringBuilder();

        int year = dataTime.getYear();
        stringBuilder.append(year+"년 ");
        int month = dataTime.getMonthValue();
        stringBuilder.append(month+"월 ");
        int dayOfMonth = dataTime.getDayOfMonth();
        stringBuilder.append(dayOfMonth+"일 ");

        stringBuilder.append(this.localDateTimeToPrettyOnlyTime(dataTime));

        return stringBuilder.toString();
    }

    private String localDateTimeToPrettyOnlyTime(final LocalDateTime dataTime){
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(dataTime.getHour() + "시");
        if(dataTime.getMinute() >0)
            stringBuilder.append(dataTime.getMinute()+" 분");

        return stringBuilder.toString();
    }

    public float timeStringToFloat(String time){

        String[] split = time.split(":");
        float timeVar = Float.parseFloat(split[0]);

        float minute = Float.parseFloat(split[1]);
        if (minute >= 30f){
            timeVar += 0.5f;
        }

        return timeVar;
    }
}
