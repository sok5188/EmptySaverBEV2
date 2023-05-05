package com.example.emptySaver.utils;

import com.example.emptySaver.domain.entity.Department;
import com.example.emptySaver.domain.entity.Subject;
import com.example.emptySaver.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class UosDepartmentAutoSaver {

    private final DepartmentRepository departmentRepository;

    public void saveAllUOSDepartment(){
        if(departmentRepository.count() >0){
            log.info("Uos Department already saved in DB");
            return;
        }

        List<Department> departsFromApiData = getDepartmentHtmlData();
        for(Department department: departsFromApiData){
            departmentRepository.save(department);
        }
    }

    private List<Department> getDepartmentHtmlData(){
        ApiData.KEY.toString();
        List<Department> departments = new ArrayList<>();
        String response ="empty";
        try{
            response = callUOSDepartmentApi();
            String[] parsedResponse =response.split("<list>");
            for (int i =1; i< parsedResponse.length ; ++i){
                Department department = buildDepartmentByHtmlData(parsedResponse[i]);
                departments.add(department);
            }
        }catch (IOException e){
            log.info("callUOSDepartmentApi error");
        }

        return departments;
    }

    private Department buildDepartmentByHtmlData(String departmentHtmlData){
        //data parsing
        String[] splitSubject = departmentHtmlData.split("<|>|/");
        List<String> splitData = new ArrayList<>();

        for (String parsed: splitSubject)
            if(!parsed.isEmpty())
                splitData.add(parsed);

        //data mapping
        Map<String, String> dataMap = new HashMap<>();
        for (int i = 0; i <splitData.size() -1 ; i+=3) {
            String dataName = splitData.get(i);
            String data = splitData.get(i+1);

            if (data.length() >=10) //단순 데이터가 아니면
                data = data.substring(8, data.length()-2);

            dataMap.put(dataName,data);
        }

        Department department = Department.builder().name(dataMap.get("dept_nm")).dept(dataMap.get("up_dept")).deptDiv(dataMap.get("colg")).subDiv(dataMap.get("dept")).build();
        return department;
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
