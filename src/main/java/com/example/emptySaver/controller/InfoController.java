package com.example.emptySaver.controller;

import com.example.emptySaver.domain.dto.CrawlDto;
import com.example.emptySaver.service.CrawlService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/info")
public class InfoController {
    private final CrawlService crawlService;
    @GetMapping("/recruiting/{pageNum}")
    @Operation(summary = "해당 페이지의 리쿠르팅 정보 조회", description = "pathVariable로 넘어온 pagenum에 해당하는 리쿠르팅 정보 목록을 조회하는 API(pageNum은 0부터 시작, 데이터가 부족하면 있는 만큼만 리턴된다)")
    public ResponseEntity<CrawlDto.res> getRecruitingInfo(@PathVariable int pageNum){
        List<CrawlDto.crawlData> pagedRecruiting = crawlService.getPagedRecruiting(pageNum);
        CrawlDto.res res = new CrawlDto.res(pagedRecruiting, pagedRecruiting.isEmpty());
        return new ResponseEntity<>(res, HttpStatus.OK);
    }
    @GetMapping("/nonSubject/{pageNum}")
    @Operation(summary = "해당 페이지의 비교과 정보 조회", description = "pathVariable로 넘어온 pagenum에 해당하는 비교과 정보 목록을 조회하는 API(pageNum은 0부터 시작, 데이터가 부족하면 있는 만큼만 리턴된다)")
    public ResponseEntity<CrawlDto.res> getNonSubjectInfo(@PathVariable int pageNum){
        List<CrawlDto.crawlData> pagedRecruiting = crawlService.getPagedNonSubjects(pageNum);
        CrawlDto.res res = new CrawlDto.res(pagedRecruiting, pagedRecruiting.isEmpty());
        return new ResponseEntity<>(res, HttpStatus.OK);
    }
}
