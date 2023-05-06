package com.example.emptySaver.domain.dto;

import lombok.Builder;
import lombok.Data;

public class SubjectDto {
    @Data
    @Builder
    public static class SubjectInfo{
        private Long id;

        private String dept; //학부(과) 정보입니다.

        private String subject_div; //교과구분 정보입니다.


        private String subject_div2; //세부영역 정보입니다.

        private String class_div; //  분반 정보입니다.

        private String subjectname; // 교과목명 정보입니다.

        private String shyr; //학년 정보입니다.


        private int credit; //학점 정보입니다.


        private String prof_nm;// 담당교수 정보입니다.

        private String class_type; // 강의 유형 정보
        private String class_nm; // 강의시간및강의실
    }

    @Data
    @Builder
    public static class SubjectSearchData{
        private String name;
        private String department;
        private String grade;
        private String professor;
    }
}
