package com.example.emptySaver.repository;

import com.example.emptySaver.domain.entity.Subject;
import com.example.emptySaver.utils.UosSubjectAutoSaver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class SubjectRepositoryTest {

    @Autowired
    private SubjectRepository subjectRepository;
    @Autowired
    private UosSubjectAutoSaver uosSubjectAutoSaver;

    @BeforeEach
    void beforeEach(){
        subjectRepository.deleteAll();
    }

    private static final String URL = "https://wise.uos.ac.kr/uosdoc/api.ApiUcrMjTimeInq.oapi";
    private static final String GET = "GET";
    /*
    @DisplayName("학교 모든 부서의 강의 저장하기")
    @Test
    void saveAllSubject(){
        uosSubjectAutoSaver.saveAllSubjectByTerm("2023","A10");
        List<Subject> subjectList = subjectRepository.findAll();

        for (Subject sub: subjectList ) {
            System.out.println(sub.toString());
        }
    }

    @DisplayName("학교 서버에서 API로 강의 내용 받아서 저장하기")
    @Test
    void subjectSaveTest(){
        Subject subject = getSubject();
        Subject savedSubject = subjectRepository.save(subject);
        assertThat(savedSubject.getSubject_nm()).isEqualTo(subject.getSubject_nm());
    }
    */
    private Subject getSubject(){
        Subject subject = null;
        try{
            String response = getResponseFromUOS();
            List<Subject> subjects = uosSubjectAutoSaver.parseSubjectsHtmlData(response);
            for (Subject sub: subjects ) {
                System.out.println(sub.toString());
            }
            subject = subjects.get(0);
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
        return subject;
    }

    private String getResponseFromUOS() throws IOException {
        Map<String,String> params= new HashMap<>(){{
            put("year", "2023");
            put("term", "A10");
            put("deptDiv", "20011");
            put("dept", "A200110111");
            put("subDept", "A200200120");
        }};
        String requestURL = uosSubjectAutoSaver.buildRequestURL(URL, params);
        System.out.println(requestURL);
        URL url = new URL(requestURL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod(GET);

        int responseCode = connection.getResponseCode();

        // 성공여부
        assertThat(responseCode).isEqualTo(200);

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream(),"EUC-KR"));
        StringBuffer stringBuffer = new StringBuffer();
        String inputLine;

        while ((inputLine = bufferedReader.readLine()) != null)  {
            stringBuffer.append(inputLine);
        }
        bufferedReader.close();

        String response = stringBuffer.toString();
        //System.out.println(response);
        return response;
    }
}