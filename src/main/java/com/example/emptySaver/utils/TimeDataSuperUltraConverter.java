package com.example.emptySaver.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class TimeDataSuperUltraConverter {
    private static final String EMPTY = "empty";

    private Map<Integer,String> intToDayMap = Map.of(0,"월",1, "화", 2,"수",3,"목",4,"금",5,"토",6,"일");

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
        stringBuilder.append(div);

        if (idx % 2>0)
            stringBuilder.append(":30");

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

}
