package com.example.emptySaver.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class SubjectDto {
    @Data
    @Builder
    @Schema(description = "강의 정보")
    public static class SubjectInfo{
        private Long id;

        @Schema(description = "학부(과) 정보입니다.")
        private String dept; //학부(과) 정보입니다.

        private String upperDivName; //상위 부서
        private String deptDiv; //by colg
        private String subDiv;  //by dept

        @Schema(description = "교과구분 정보입니다.")
        private String subject_div; //교과구분 정보입니다.


        @Schema(description = "세부영역 정보")
        private String subject_div2; //세부영역 정보입니다.

        @Schema(description = "분반 정보")
        private String class_div; //  분반 정보입니다.

        @Schema(description = "교과목명 정보")
        private String subjectname; // 교과목명 정보입니다.

        @Schema(description = "학년 정보")
        private String shyr; //학년 정보입니다.


        @Schema(description = "학점 정보")
        private int credit; //학점 정보입니다.


        @Schema(description = "담당교수 정보")
        private String prof_nm;// 담당교수 정보입니다.

        @Schema(description = "강의 유형")
        private String class_type; // 강의 유형 정보
        @Schema(description = "강의시간및강의실")
        private String class_nm; // 강의시간및강의실
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor

    @Schema(description = "강의 검색 form<br>"+"강의 이름(name)으로 검색시, 해당 이름이 들어간 모든 강의가 검색됩니다.  <br>" +
            "강의 이름(name)을 담으면, department(학과)와 grade(학년)정보는 담지 않아도 됩니다." +
            "반대로 강의 이름 대신 다른걸로 검색할때는 department(학과)와 grade(학년)정보 두가지 모두 담아서 보내야합니다.")
    public static class SubjectSearchData{

        @Schema(description = "강의 이름, 한 글자만 적어도 매칭되는것 검색가능")
        private String name;
        @Schema(description = "강의 부서, 한 글자만 적어도 매칭되는것 검색가능")
        private String department;

        @Schema(description = "강의 학년, 1~4 사이의 숫자로")
        private String grade;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "학과 정보에 대해서")
    public static class DepartmentDto{
        private Long id;

        private String name;
        private String deptDiv; //by colg
        private String dept;    //by up_dept
        private String subDiv;  //by dept
    }
}
