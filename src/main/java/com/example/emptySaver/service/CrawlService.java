package com.example.emptySaver.service;

import com.example.emptySaver.domain.dto.CrawlDto;
import com.example.emptySaver.domain.entity.NonSubject;
import com.example.emptySaver.domain.entity.Recruiting;
import com.example.emptySaver.domain.entity.category.Category;
import com.example.emptySaver.repository.NonSubjectRepository;
import com.example.emptySaver.repository.RecruitingRepository;
//import jakarta.transaction.Transactional;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.URL;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CrawlService {
    private final RecruitingRepository recruitingRepository;
    private final NonSubjectRepository nonSubjectRepository;
    private final CategoryService categoryService;
    private final ScheduleService scheduleService;
    String userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/112.0.0.0 Safari/537.36";
    @Value("${portal.id}")
    String id;
    @Value("${portal.password}")
    String password;
    Map<String,String> formData=new HashMap<>();
    Map<String,String> sameHeader=new HashMap<>();
    Connection.Response first;


    @PostConstruct
    @Transactional
    public void CrawlService() throws IOException {
        //깃헙올라갈때 빌드 오류 방지
        if(id.equals("fake"))
            return;
        this.InitCrawl();
        this.CrawlRecruiting();
        this.CrawlNonSubject();
        this.CrawlMovie();
    }

    @Scheduled(cron = "0 0 5 * * *",zone = "Asia/Seoul")
    @Transactional
    public void ScheduleCrawl() throws IOException {
        log.info("Schedule Called Crawl Uostory!");
        this.InitCrawl();
        this.CrawlRecruiting();
        this.CrawlNonSubject();
    }
    @Scheduled(cron = "0 0 7 * * *",zone = "Asia/Seoul")
    @Transactional
    public void MovieCrawl1() throws IOException {
        log.info("crawl movie 1");
        this.CrawlMovie();
    }
    @Scheduled(cron = "0 0 8 * * *",zone = "Asia/Seoul")
    @Transactional
    public void MovieCrawl2() throws IOException {
        log.info("crawled movie 2");
        this.CrawlMovie();
    }
    public void InitCrawl() throws IOException{
        log.info("crawl construct start");
        formData.put("_enpass_login_","submit");
        formData.put("langKnd","ko");
        formData.put("loginType","normal");
        formData.put("returnUrl","https://uostory.uos.ac.kr/");
        formData.put("ssoId",id);
        formData.put("password",password);

        sameHeader.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7");
        sameHeader.put("Accept-Encoding", "gzip, deflate, br");
        sameHeader.put("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7");

        first= Jsoup.connect("https://uostory.uos.ac.kr/index.jsp").method(Connection.Method.GET)
                .followRedirects(false)
                .userAgent(userAgent).headers(sameHeader).execute()
        ;

        Connection.Response response=Jsoup.connect("https://portal.uos.ac.kr/user/loginProcess.face").userAgent(userAgent).timeout(5000).data(formData)
                .method(Connection.Method.POST).headers(sameHeader).execute();
        System.out.println("Now again index.jsp ");
        Connection.Response indexJSPFirst = Jsoup.connect("https://uostory.uos.ac.kr/index.jsp").method(Connection.Method.GET)
                .cookie("JSESSIONID",first.cookie("JSESSIONID"))
                .followRedirects(false)
                .userAgent(userAgent).headers(sameHeader).execute()
                ;
        System.out.println("Now PSSO ");
        Connection.Response psso = Jsoup.connect("https://psso.uos.ac.kr/enpass/login?gateway=client&service=https://uostory.uos.ac.kr/index.jsp")
                .method(Connection.Method.GET).followRedirects(false).userAgent(userAgent).headers(sameHeader)
                .cookie("ENPASSTGC",response.cookie("ENPASSTGC")).cookie("JSESSIONID",response.cookie("JSESSIONID"))
                .execute();
        System.out.println("Now in index jsp with epTicket");
        String location = psso.headers().get("Location");
        URL url = new URL(location);
        String query = url.getQuery();
        System.out.println("query:"+query);
        Connection.Response indexJSPWithTicket = Jsoup.connect("https://uostory.uos.ac.kr/index.jsp?" + query).method(Connection.Method.GET).followRedirects(true)
                .userAgent(userAgent).headers(sameHeader).cookie("JSESSIONID",first.cookie("JSESSIONID")).execute();
        System.out.println("Now in logon");
        Connection.Response logon = Jsoup.connect("https://uostory.uos.ac.kr/site/member/logon").method(Connection.Method.GET).followRedirects(true)
                .userAgent(userAgent).headers(sameHeader).header("Referer", String.valueOf(indexJSPWithTicket.url())).cookie("JSESSIONID",first.cookie("JSESSIONID")).execute();
    }

//    @Transactional
    public void CrawlRecruiting() throws IOException {
        recruitingRepository.deleteAll();
        boolean isFin=false;
        int i=1;
        List<Recruiting> recruitingList=new ArrayList<>();

        while (!isFin&&i<10){
//            System.out.println("now iter:"+i);
            String url="https://uostory.uos.ac.kr/site/reservation/lecture/lectureList?menuid=001003002002&reservegroupid=1&viewtype=L&rectype=J&thumbnail=Y&currentpage="+i++;
            Document document = Jsoup.connect(url)
                    .userAgent(userAgent).headers(sameHeader).cookie("JSESSIONID", first.cookie("JSESSIONID")).get();
            Elements ul = document.select("#searchForm > div.list_tyle_h1.mt10 > ul");
            System.out.println("ul selected");
            Elements li = ul.select("li");

            for (Element liElement : li) {
                //li : 정보 1개
                Elements tbody = liElement.select("tbody");
                Elements trs = tbody.select("tr");
                boolean isPass=false;
                Recruiting recruiting=new Recruiting();
                for (int k = 0; k < trs.size(); k++) {
                    Element tr = trs.get(k);
//                    System.out.println(tr.text());
                    if(k==1){
                        //신청가능여부
                        if(!tr.text().equals("신청가능")){
                            System.out.println("Not Possible.. now tr="+tr.text());
                            isFin=isPass=true;
                            break;
                        }
                        else continue;
                    }
                    String[] s = tr.text().split(" ");
                    String tagName = s[0];
                    String tagValue = "";
                    if (s.length > 1) {
                        for (int j = 1; j < s.length; j++) {
                            tagValue += s[j] + " ";
                        }
                    }
                    if (tagName.equals("과정명")) {
                        Elements a = tr.select("a");
                        String href = a.attr("href");
                        String substring = href.substring(1, href.length());
                        String hrefUrl = "https://uostory.uos.ac.kr/site/reservation/lecture" + substring;
                        recruiting.setUrl(hrefUrl);
                        recruiting.setCourseName(tagValue);
                    } else if(tagName.equals("신청기간")){
                        recruiting.setApplyDate(tagValue);
                    } else if(tagName.equals("운영기간")){
                        recruiting.setRunDate(tagValue);
                    } else if(tagName.equals("대상학과"))
                        recruiting.setTargetDepartment(tagValue);
                    else if(tagName.equals("대상학년"))
                        recruiting.setTargetGrade(tagValue);
                }
                System.out.println("fin trs");
                if(!isPass) {
                    recruitingList.add(recruiting);
                }

            }
            System.out.println("fin li");
            System.out.println("Now fin flag:"+isFin);
        }
        recruitingRepository.saveAll(recruitingList);

    }
//    @Transactional
    public void CrawlNonSubject() throws IOException {
        nonSubjectRepository.deleteAll();
        List<NonSubject> nonSubjectList=new ArrayList<>();

        for(int i=1;i<10;i++){
            String url="https://uostory.uos.ac.kr/site/reservation/lecture/lectureList?menuid=001004002001&reservegroupid=1&viewtype=L&rectype=L&thumbnail=Y&currentpage="+i;
            Document document = Jsoup.connect(url)
                    .userAgent(userAgent).headers(sameHeader).cookie("JSESSIONID", first.cookie("JSESSIONID")).get();
            Elements ul = document.select("#searchForm > div.list_tyle_h1.mt10 > ul");
            System.out.println("ul selected");
            Elements li = ul.select("li");

            for (Element liElement : li) {
                //li : 정보 1개
                Elements tbody = liElement.select("tbody");
                Elements trs = tbody.select("tr");
                boolean isPass=false;
                NonSubject nonSubject = new NonSubject();
                for (int k = 0; k < trs.size(); k++) {
                    Element tr = trs.get(k);
                    System.out.println(tr.text());
                    if(k==1){
                        //신청가능여부
                        if(!tr.text().equals("신청가능")){
                            isPass=true;
                            break;
                        }
                        else continue;
                    }
                    String[] s = tr.text().split(" ");
                    String tagName = s[0];
                    String tagValue = "";
                    if (s.length > 1) {
                        for (int j = 1; j < s.length; j++) {
                            tagValue += s[j] + " ";
                        }
                    }
                    if (tagName.equals("과정명")) {
                        Elements a = tr.select("a");
                        String href = a.attr("href");
                        String substring = href.substring(1, href.length());
                        String hrefUrl = "https://uostory.uos.ac.kr/site/reservation/lecture" + substring;
                        nonSubject.setUrl(hrefUrl);
                        nonSubject.setCourseName(tagValue);
                    } else if(tagName.equals("신청기간")){
                        nonSubject.setApplyDate(tagValue);
                    } else if(tagName.equals("운영기간")){
                        nonSubject.setRunDate(tagValue);
                    } else if(tagName.equals("대상학과"))
                        nonSubject.setTargetDepartment(tagValue);
                    else if(tagName.equals("대상학년"))
                        nonSubject.setTargetGrade(tagValue);
                }
                if(!isPass) {
                    nonSubjectList.add(nonSubject);
                }

            }
        }
        nonSubjectRepository.saveAll(nonSubjectList);

    }



    public List<CrawlDto.crawlData> getPagedNonSubjects(int pageNum){
        Page<NonSubject> pages = nonSubjectRepository.findAll(PageRequest.of(pageNum, 15));
        List<CrawlDto.crawlData> result=new ArrayList<>();
        pages.toList().stream().forEach( n->result.add(CrawlDto.crawlData.builder().courseName(n.getCourseName())
                        .applyDate(n.getApplyDate()).runDate(n.getRunDate()).targetDepartment(n.getTargetDepartment())
                        .targetGrade(n.getTargetGrade()).url(n.getUrl())
                .build()));
        return result;
    }
    public List<CrawlDto.crawlData> getPagedRecruiting(int pageNum){
        Page<Recruiting> pages = recruitingRepository.findAll(PageRequest.of(pageNum, 15));
        List<CrawlDto.crawlData> result=new ArrayList<>();
        pages.toList().stream().forEach( n->result.add(CrawlDto.crawlData.builder().courseName(n.getCourseName())
                .applyDate(n.getApplyDate()).runDate(n.getRunDate()).targetDepartment(n.getTargetDepartment())
                .targetGrade(n.getTargetGrade()).url(n.getUrl())
                .build()));
        return result;
    }

    public void CrawlMovie() throws IOException {
        scheduleService.deleteAllSavedMovieBefore();    //과거 영화 지우고 시작

        Document document = Jsoup.connect("https://search.naver.com/search.naver?where=nexearch&sm=top_hty&fbm=0&ie=utf8&query=%EB%A1%AF%EB%8D%B0%EC%8B%9C%EB%84%A4%EB%A7%88+%EC%B2%AD%EB%9F%89%EB%A6%AC+%EC%83%81%EC%98%81")
                .userAgent(userAgent).headers(sameHeader).get();
        Elements select = document.select("#main_pack > section.sc_new.cs_movie_house > div > div.api_cs_wrap._theater_search_container > div._wrap_single_type > div.movie_content._wrap_time_table > div > div.list_tbl_box > table > tbody");
        Elements trs = select.select("tr");
        List<TmpMovie> movieList = new ArrayList<>();
        for (Element tr : trs) {
            Elements a = tr.select("th > a");
            String href = a.attr("href");
            // TODO : 영화 상세 정보 URL
            String movieInfoUrl="https://search.naver.com/search.naver"+href;
            System.out.println("url : "+movieInfoUrl);
            // TODO: 영화 상세 보기 페이지에서 상영시간 parsing하기
            Elements detailInfo=parseDetail(movieInfoUrl);
            // TODO : 일단 5번 시도해도 안긁어와지면 걍 넘어감
            if(detailInfo==null)
                continue;
            System.out.println("detail Info : "+detailInfo.html());
            String[] split = detailInfo.html().split("<span class=\"cm_bar_info\"></span>");
            String movieGenre=split[0];
            String movieCountry=split[1];
//            System.out.println("Info : genre:"+movieGenre+ " / country : "+movieCountry + " / runningTime : "+split[2]);
//            System.out.println(split[2].replace("분",""));

            int movieRunningTime= Integer.parseInt(split[2].replace("분","")); //런타임 정수화
            System.out.println("Info : genre:"+movieGenre+ " / country : "+movieCountry + " / runningTime : "+movieRunningTime);


            Category targetCategory;

            try{
                //영화 장르로 Category객체 찾아옴 ( 만약 해당 장르가 없으면 예외를던짐
                targetCategory = categoryService.getCategoryByLabel(movieGenre);
            }
            catch (Exception e){
                //그렇기에 기타 장르로 설정합시다.
                // DB에 카테고리 정보가 제대로 저장되어 있지 않다면 여기서 또 에러를 던지니 주의..
                Optional<? extends Category> etc = categoryService.getListByCategoryAndLabel("movie", "기타");
                if(!etc.isPresent()){
                    log.error("========================================");
                    log.error("Category Movie doesnt have etc column.. Check The DataBase!");
                    log.error("========================================");
                    //return;
                }else{
                    targetCategory=etc.get();
                }
            }

            String title=a.text();
            Elements divs = tr.select("td > div");
            List<RoomInfo> roomInfoList = new ArrayList<>();
            log.info("divs: " + divs.size());
            for (Element div : divs) {
                //TODO: 상영관 정보 ex, (2관) -> () 는 사용할거면 지워야할듯
                String movieRoomNum = div.select("span.place").text();
                System.out.println("span = " + movieRoomNum);


                Elements as = div.select("a");
                List<MovieTimeInfo> movieTimeInfoList = new ArrayList<>();
                for (Element target : as) {
                    // TODO : 해당 상영관에서 해당 영화가 상영되는 시간 및 예매 페이지 href
                    movieTimeInfoList.add(new MovieTimeInfo(target.text(),target.attr("href")));
                }
                roomInfoList.add(new RoomInfo(movieRoomNum,movieTimeInfoList));
            }
            TmpMovie tmpMovie = new TmpMovie(title, movieInfoUrl, roomInfoList, movieRunningTime);
//            System.out.println("add movie : "+tmpMovie);
//            System.out.println("===============================================");
            movieList.add(tmpMovie);
        }
        /*
        log.info("movieList size: "+ movieList.size());
        for (TmpMovie tmpMovie : movieList) {
            System.out.println("---------Movie Info-------");
            System.out.println(tmpMovie);
            //log.info(tmpMovie.toString());
        }*/
        scheduleService.saveMovieScheduleList(movieList);
        //return movieList;
    }

    private Elements parseDetail(String movieInfoUrl) throws IOException {
        int count=5;
        while (count-->0){
            Document movieInfoDoc = Jsoup.connect(movieInfoUrl).userAgent(userAgent).headers(sameHeader).get();
//            System.out.println("movieInfoDoc = " + movieInfoDoc);
            Elements detailInfo = movieInfoDoc.selectXpath("//*[@id=\"main_pack\"]/div[2]/div[2]/div[1]/div[2]/div[2]/dl/div[1]/dd");
            ///html/body/div[3]/div[2]/div/div[1]/div[2]/div[2]/div[1]/div[2]/div[2]/dl/div[1]/dd
            ///html/body/div[3]/div[2]/div/div[1]/div[2]/div[2]/div[1]/div[2]/div[2]/dl/div[1]/dd
            ////*[@id="main_pack"]/div[2]/div[2]/div[1]/div[2]/div[2]/dl/div[1]/dd
            ////*[@id="main_pack"]/div[2]/div[2]/div[1]/div[2]/div[2]/dl/div[1]/dd
            System.out.println("detailInfo = " + detailInfo);
            if(detailInfo.text().length()>3){
                return detailInfo;
            }
            System.out.println("Failed to load movieInfo.. Try Again");

        }
        System.out.println("All fail.. skip this movie");
        return null;
    }

    @Builder
    @ToString
    static class TmpMovie{
        String title;
        String movieUrl;
        List<RoomInfo> roomInfoList;
        Integer runningTime;
    }
    @Builder
    @ToString
    static class RoomInfo{
        String movieRoomNum;
        List<MovieTimeInfo> timeInfoList;
    }
    @Builder
    @ToString
    static class MovieTimeInfo{
        String time;
        String reservationUrl;
    }


}
