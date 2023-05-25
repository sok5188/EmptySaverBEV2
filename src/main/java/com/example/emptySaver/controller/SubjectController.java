package com.example.emptySaver.controller;

import com.example.emptySaver.domain.dto.SubjectDto;
import com.example.emptySaver.service.MemberService;
import com.example.emptySaver.service.SubjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/subject")
@RequiredArgsConstructor
@Slf4j
public class SubjectController {
    private final SubjectService subjectService;
    private final MemberService memberService;

    @PostMapping("/search")
    @Operation(summary = "강의 검색", description = "검색 데이터 form을 작성해 보내면 반환해줌.<br>"+ "일단 간단한 검색만 구현했습니다. <br>" +
            "강의 이름(name)으로 검색시, 해당 이름이 들어간 모든 강의가 검색됩니다.  <br>" +
            "강의 이름(name)을 담으면, department(학과)와 grade(학년)정보는 담지 않아도 됩니다." +
            "반대로 강의 이름 대신 다른걸로 검색할때는 department(학과)와 grade(학년)정보 두가지 모두 담아서 보내야합니다.")
    public ResponseEntity<List<SubjectDto.SubjectInfo>> searchSubjectByKeyword(@RequestBody SubjectDto.SubjectSearchData searchData){
        List<SubjectDto.SubjectInfo> searchedSubjects = subjectService.getSearchedSubjects(searchData);
        return new ResponseEntity<>(searchedSubjects, HttpStatus.OK);
    }

    @PostMapping("/saveSubjectToMember")
    @Operation(summary = "강의를 스케줄로 저장하기", description = "로그인한 상태에서 강의를 자신의 스케줄로 추가")
    @Parameter(
            name = "subjectId",
            description = "subjectId 검색된 강의의 Id를 그대로 사용해서 Uri에 담아주세요. <br />" +
                    "만약 DB에 존재하지 않는 subjectId를 보내면 저장에 실패합니다. <br />" +
                    "강의를 저장하면 Member의 시간표에 주기적 스케줄로 저장됩니다. <br />" +
                    "따라서 시간표에 저장된 강의는 수정,삭제는 일반 Schedule처럼하면 됩니다."
    )
    public ResponseEntity<String> saveSubjectToMemberTimeTable(final @RequestParam Long subjectId){
        Long currentMemberId = memberService.getCurrentMemberId();
        subjectService.saveSubjectToMemberSchedule(currentMemberId,subjectId);
        return new ResponseEntity<>("save Subject to Member, subjectId: " + subjectId, HttpStatus.OK);
    }

    @GetMapping("/getAllDepartment")
    @Operation(summary = "모든 부서 정보 가져오기", description = "DB에 존재하는 모든 부서의 정보를 가져옴")
    public ResponseEntity<List<SubjectDto.DepartmentDto>>getAllDepartmentDto(){
        return new ResponseEntity<>(subjectService.getAllDepartment(), HttpStatus.OK);
    }
    @GetMapping("/getAllDivInfoList")
    @Operation(summary = "상위 대학 정보 리스트 반환", description = "공대, 정경대 이런 상위 대학 정보 목록을 리턴하는 API")
    public ResponseEntity<List<SubjectDto.DivInfo>> getAllUpperDivList(){
        return new ResponseEntity<>(subjectService.getDivInfoList(),HttpStatus.OK);
    }
}
