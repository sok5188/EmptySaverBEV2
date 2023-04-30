package com.example.emptySaver;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Component
@Aspect
@Slf4j
public class TimeTraceAOP {
    @Around("execution(* com.example.emptySaver..*(..))")
    public Object execute(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        log.info("Time Start:"+joinPoint.toString());

        try {
            return joinPoint.proceed();
        }finally {
            long fin=System.currentTimeMillis();
            long time=fin-start;
            log.info("End:"+joinPoint.toString()+" Time:"+time+"ms");
        }
    }
}