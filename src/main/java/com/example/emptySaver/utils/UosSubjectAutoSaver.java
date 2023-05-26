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
import java.time.LocalDate;
import java.time.LocalDateTime;
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
        final List<Department> departmentList = departmentRepository.findAll();

        Map<String,Department> departmentMap= new HashMap<>();
        for (Department department : departmentList) {
            String name = department.getName();
            departmentMap.put(name, department);
        }

        Map<String,String> params= new HashMap<>(){
            {put("year", year); put("term", term);}};


        //모든 학과 대상 호출
        for (Department depart:departmentList){
            log.info(depart.toString());
            params.put("deptDiv", depart.getDeptDiv());
            params.put("dept", depart.getDept());
            params.put("subDept", depart.getSubDiv());
            params.put("upperDivName", depart.getUpperDivName());

            String requestURL = buildRequestURL(ApiData.SUBJECT_URL.getData(), params);
            String response = getResponseFromSubjectApi(requestURL);
            if(response.equals(ApiData.ERROR.getData()))   //api call error 발생
                continue;

            List<Subject> subjects = parseSubjectsHtmlData(response, params);
            this.setSubjectStartEndTime(subjects,year, term);
            subjectRepository.saveAll(subjects);
        }

        saveAllCultureSubjectByTerm(departmentMap, year, term,"A01");    //교양선택시간표 저장장
        saveAllCultureSubjectByTerm(departmentMap, year, term,"A01");    //교양필수시간표 저장장
    }

    public void saveAllCultureSubjectByTerm(final Map<String,Department> departmentMap,final String year, final String term, final String subjectDiv){
        Map<String,String> params= new HashMap<>(){
            {put("year", year); put("term", term); put("subjectDiv", subjectDiv);}};

        String requestURL = buildRequestURL(ApiData.CULTURE_SUBJECT_URL.getData(), params);
        String response = this.getResponseFromSubjectApi(requestURL);
        if(response.equals(ApiData.ERROR.getData()))   //api call error 발생
            return;

        List<Subject> subjects = this.parseSubjectsHtmlData(response, params);
        this.setSubjectStartEndTime(subjects,year, term);
        this.setCultureSubjectUpDeptName(departmentMap, subjects);
        List<Subject> subjects1 = subjectRepository.saveAll(subjects);
        for (Subject subject : subjects1) {
            log.info(subject.getSubjectname() + ", " + subject.getDept() + " up " + subject.getUpperDivName());
        }
    }

    private void setCultureSubjectUpDeptName(final Map<String,Department> departmentMap, List<Subject> subjects){
        for (Subject subject : subjects) {
            String deptName = subject.getDept();
            if(!departmentMap.containsKey(deptName))
                continue;   //없으면 건너뛰

            Department department = departmentMap.get(deptName);
            subject.setUpperDivName(department.getUpperDivName());
        }
    }

    private void setSubjectStartEndTime(List<Subject> subjects, String year, String term){
        int yearToInt = Integer.parseInt(year);
        int startMonth = 3, termMonthNum = 4;
        int startDay = 1;
        if(term.equals("A20")){
            startMonth = 9;
        }

        LocalDateTime startTime = LocalDate.of(yearToInt, startMonth, startDay).atStartOfDay();
        LocalDateTime endTime = startTime.plusMonths(4).minusDays(1);

        for (Subject subject : subjects) {
            subject.setStartTime(startTime);
            subject.setEndTime(endTime);
        }
    }

    private String getResponseFromSubjectApi(String requestURL){
        String ret;
        try{
            ret = getResponseFromUOS(requestURL);
        }catch (IOException e){
            log.info("UOS Subject api error");
            ret = ApiData.ERROR.getData();
        }
        return ret;
    }

    private String getResponseFromUOS( String requestURL) throws IOException {
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

    public List<Subject> parseSubjectsHtmlData(String subjectHtmlData, Map<String,String> params){
        String[] splitSubject = subjectHtmlData.split("<list>");

        List<Subject> subjects = new ArrayList<>();
        for (int i=1;i<splitSubject.length;++i)
            subjects.add(buildSubjectByHtmlData(splitSubject[i], params));

        return  subjects;
    }

    private Subject buildSubjectByHtmlData(String subjectHtmlData, Map<String,String> params){
        //data parsing
        String[] splitSubject = subjectHtmlData.split("</|<|>");
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
        subject.setSubjectname(dataMap.get("subject_nm"));
        subject.setDept(dataMap.get("sub_dept"));
        subject.setSubject_div(dataMap.get("subject_div2"));
        subject.setSubject_div2(dataMap.get("subject_div2"));
        subject.setClass_div(dataMap.get("class_div"));
        subject.setCredit(credit);
        subject.setShyr(dataMap.get("shyr"));
        subject.setProf_nm(dataMap.get("prof_nm"));
        subject.setClass_nm(dataMap.get("class_nm"));
        subject.setClass_type(dataMap.get("class_type"));
        subject.setYears(dataMap.get("year"));
        subject.setTerm(dataMap.get("term"));

        subject.setDeptDiv(params.get("deptDiv"));  //세부 구분도
        subject.setSubDiv(params.get("subDept"));
        subject.setUpperDivName(params.get("upperDivName"));


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
            String timeData = data.substring(1,data.length());

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
