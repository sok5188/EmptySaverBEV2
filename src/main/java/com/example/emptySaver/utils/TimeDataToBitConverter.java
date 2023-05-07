package com.example.emptySaver.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

@Component
@Slf4j
public class TimeDataToBitConverter {

    public Long convertTimeToBit(LocalDateTime startTime, LocalDateTime endTime){
        Long retBits = 0l;

        int startIdx = startTime.getHour()*2 + (startTime.getMinute()/30);
        int endIdx = endTime.getHour()*2 + (endTime.getMinute()/30);
        System.out.println("st: "+ startIdx + ", end: " + endIdx );
        Long bits = 1l;
        bits <<= (startIdx);
        for(int i =0; i<(endIdx - startIdx) ; ++i){
            retBits |= bits;
            bits <<=1;
        }

        return retBits;
    }

}
