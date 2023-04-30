package com.example.emptySaver.utils;

import com.example.emptySaver.domain.entity.Department;
import com.example.emptySaver.domain.entity.Subject;
import com.example.emptySaver.repository.DepartmentRepository;
import com.example.emptySaver.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Component
public class UosSubjectAutoSaver {
    private final SubjectRepository subjectRepository;
    private final DepartmentRepository departmentRepository;
    private final UosDepartmentAutoSaver uosDepartmentAutoSaver;

    private final Map<String,Integer> dayToInt = new HashMap<>(){{
            put("월", 0); put("화", 1);
            put("수", 2); put("목", 3);
            put("금", 4); put("토", 5);
            put("일", 6);
        }};

    public String buildRequestURL(String url, Map<String, String> params){
        StringBuilder stringBuilder = new StringBuilder(url);
        stringBuilder.append("?apiKey=" + ApiData.KEY.getData());

        Set<String> keySet = params.keySet();
        for ( String key : keySet) {
            String value = params.get(key);
            stringBuilder.append("&" + key + "=" + value);
        }

        return stringBuilder.toString();
    }

    public void saveAllSubjectByTerm(String year, String term){
        uosDepartmentAutoSaver.saveAllUOSDepartment();
        List<Department> departmentList = departmentRepository.findAll();

        Map<String,String> params= new HashMap<>(){
            {put("year", year); put("term", term);}};

        //모든 학과 대상 호출
        for (Department depart:departmentList){
            log.info(depart.toString());
            params.put("deptDiv", depart.getDeptDiv());
            params.put("dept", depart.getDept());
            params.put("subDept", depart.getSubDiv());

            String response = getResponseFromSubjectApi(params);
            if(response.equals(ApiData.ERROR.getData()))   //api call error 발생
                continue;

            List<Subject> subjects = parseSubjectsHtmlData(response);
            subjectRepository.saveAll(subjects);
        }
    }

    private String getResponseFromSubjectApi(Map<String,String> params){
        String ret;
        try{
            ret = getResponseFromUOS(params);
        }catch (IOException e){
            log.info("UOS Subject api error");
            ret = ApiData.ERROR.getData();
        }
        return ret;
    }

    private String getResponseFromUOS(Map<String,String> params) throws IOException {
        String requestURL = buildRequestURL(ApiData.SUBJECT_URL.getData(), params);
        log.info(requestURL);
        URL url = new URL(requestURL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod(ApiData.GET.getData());

        int responseCode = connection.getResponseCode();
        log.info(""+responseCode);

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

    public List<Subject> parseSubjectsHtmlData(String subjectHtmlData){
        String[] splitSubject = subjectHtmlData.split("<list>");

        List<Subject> subjects = new ArrayList<>();
        for (int i=1;i<splitSubject.length;++i)
            subjects.add(buildSubjectByHtmlData(splitSubject[i]));

        return  subjects;
    }

    private Subject buildSubjectByHtmlData(String subjectHtmlData){
        //data parsing
        String[] splitSubject = subjectHtmlData.split("<|>|/");
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

        int credit = -1;
        try {
            credit= Integer.parseInt(dataMap.get("credit"));
        } catch (NumberFormatException exception){
            credit = -1;
        }

        //Subject subject = Subject.builder().subject_nm(dataMap.get("subject_nm")).credit(Integer.parseInt(dataMap.get("credit"))).build();
        /*
        Subject subject1 = Subject.builder()
                .subject_nm(dataMap.get("subject_nm"))
                .sub_dept(dataMap.get("sub_dept"))
                .subject_div(dataMap.get("subject_div"))
                .subject_div2(dataMap.get("subject_div2"))
                .class_div(dataMap.get("class_div"))
                .credit(credit)
                .shyr(dataMap.get("shyr"))
                .prof_nm(dataMap.get("prof_nm"))
                .year(dataMap.get("year"))
                .term(dataMap.get("term"))
                .build();*/
        Subject subject = new Subject();
        subject.setSubject_nm(dataMap.get("subject_nm"));
        subject.setSub_dept(dataMap.get("sub_dept"));
        subject.setSubject_div(dataMap.get("subject_div2"));
        subject.setSubject_div2(dataMap.get("subject_div2"));
        subject.setClass_div(dataMap.get("class_div"));
        subject.setCredit(credit);
        subject.setShyr(dataMap.get("shyr"));
        subject.setProf_nm(dataMap.get("prof_nm"));
        subject.setYears(dataMap.get("year"));
        subject.setTerm(dataMap.get("term"));

        subject.setWeekScheduleData(class_numToSchedule(dataMap.get("class_nm")));  //상위 클래스 데이터이므로 직접 삽입
        return subject;
    }

    private long[] class_numToSchedule(String classTimeData){
        //ex: 월02,03/19-110/111, 목05,06/19-110/111
        long[] schedule = {0,0,0,0,0,0,0};
        if (classTimeData == null) {
            return schedule;
        }

        String[] dayTimeDataList = classTimeData.split(", ");

        for(String dayTimeData: dayTimeDataList){
            String data = dayTimeData.split("/")[0];
            if(data.length()<2){
                continue;
            }

            String day = data.substring(0,1);
            String timeData = data.substring(1);

            if(!dayToInt.containsKey(day))
                continue;

            schedule[dayToInt.get(day)] = makeScheduleByStringData(timeData);
        }

        return schedule;
    }

    private long makeScheduleByStringData(String timeData){
        String[] times = timeData.split(",");

        int subjectStartTimeIdx = Integer.parseInt(times[0]);
        subjectStartTimeIdx += 8;
        subjectStartTimeIdx *= 2;

        int subjectEndTimeIdx = Integer.parseInt(times[times.length -1]);
        subjectEndTimeIdx += 9;
        subjectEndTimeIdx *= 2;
        //log.info("" + subjectStartTimeIdx+" to " + subjectEndTimeIdx);

        long fillBits = 0;
        long tempBits = 1;
        for (int i = subjectStartTimeIdx ; i < subjectEndTimeIdx; i++) {
            tempBits =1;
            tempBits <<= i;
            fillBits |= tempBits;
        }
        //log.info(Long.toBinaryString(fillBits));

        return fillBits;
    }

}
