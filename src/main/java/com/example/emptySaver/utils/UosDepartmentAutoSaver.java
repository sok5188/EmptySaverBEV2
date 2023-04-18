package com.example.emptySaver.utils;

import com.example.emptySaver.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class UosDepartmentAutoSaver {

    private final DepartmentRepository departmentRepository;

    public void saveAllUOSDepartment(){
        log.info(getDepartmentHtmlData());
    }

    private String getDepartmentHtmlData(){
        ApiData.KEY.toString();
        String response ="empty";
        try{
            response = callUOSDepartmentApi();
        }catch (IOException e){
            log.info("callUOSDepartmentApi error");
        }
        return response;
    }

    private String callUOSDepartmentApi() throws IOException {
        String requestURL = "https://wise.uos.ac.kr/uosdoc/api.ApiApiDeptList.oapi?apiKey=202303727IDO15243&openYn=Y";
        System.out.println(requestURL);
        URL url = new URL(requestURL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();

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
