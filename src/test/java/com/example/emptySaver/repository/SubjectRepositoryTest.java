package com.example.emptySaver.repository;

import com.example.emptySaver.utils.UosApiBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class SubjectRepositoryTest {

    @Autowired
    private SubjectRepository subjectRepository;
    @Autowired
    private UosApiBuilder uosApiBuilder;

    @BeforeEach
    void beforeEach(){
        subjectRepository.deleteAll();
    }

    private static final String URL = "https://wise.uos.ac.kr/uosdoc/api.ApiUcrMjTimeInq.oapi";
    private static final String GET = "GET";

    @DisplayName("학교 서버에서 API로 강의 내용 받아서 저장하기")
    @Test
    void apiTest() throws IOException {
        Map<String,String> params= new HashMap<>(){{
            put("year", "2023");
            put("term", "A10");
            put("deptDiv", "20011");
            put("dept", "A200110111");
            put("subDept", "A200200120");
        }};
        String requestURL = uosApiBuilder.buildRequestURL(URL, params);
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
        System.out.println(response);
    }
}