package com.example.emptySaver.controller;

import com.example.emptySaver.domain.dto.SubjectDto;
import com.example.emptySaver.service.SubjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/subject")
@RequiredArgsConstructor
@Slf4j
public class SubjectController {
    private final SubjectService subjectService;

    @PostMapping("/search")
    @Operation(summary = "강의 검색", description = "")
    @Parameter(
            name = "requestForm",
            description = "startDate와 endDate는 날짜 정보만을 필요로 한다.\n"

    )
    public ResponseEntity<String> searchSubjectByKeyword(@RequestBody SubjectDto.SubjectSearchData searchData){
        return new ResponseEntity<>("dd", HttpStatus.OK);
    }
}
