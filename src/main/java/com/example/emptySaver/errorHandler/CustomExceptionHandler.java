package com.example.emptySaver.errorHandler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class CustomExceptionHandler {
    @ExceptionHandler(value = {BaseException.class})
    protected ResponseEntity<BaseResponse<BaseResponseStatus>> customExceptionHandler(BaseException e){
       log.error("Exception :"+e.getStatus().getMessage()+" / code: "+e.getStatus().getCode());
       return new ResponseEntity<>(new BaseResponse<>(e.getStatus()), HttpStatus.BAD_REQUEST);
    }
}
