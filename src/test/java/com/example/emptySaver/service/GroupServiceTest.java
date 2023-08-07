package com.example.emptySaver.service;

import com.example.emptySaver.domain.entity.Member;
import com.example.emptySaver.domain.entity.MemberTeam;
import com.example.emptySaver.domain.entity.Team;
import com.example.emptySaver.domain.entity.category.Movie;
import com.example.emptySaver.domain.entity.category.MovieType;
import com.example.emptySaver.repository.CategoryRepository;
import com.example.emptySaver.repository.MemberRepository;
import com.example.emptySaver.repository.MemberTeamRepository;
import com.example.emptySaver.repository.TeamRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@SpringBootTest
@ActiveProfiles("local")
@Slf4j
public class GroupServiceTest {
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    GroupService groupService;
    @Autowired
    TeamRepository teamRepository;
    @Autowired
    MemberTeamRepository memberTeamRepository;


    @Test
    void joinError() throws InterruptedException {
        Member member = makeMember();
        Team team = makeTeam(member);
        log.info("made member and team");
        CountDownLatch countDownLatch = new CountDownLatch(10);
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        for (int i = 0; i < 10; i++) {
            executorService.execute(()->{
                log.info("execute Service Logic..");
                Member target = makeMember();
                MemberTeam mt = new MemberTeam();
                mt.initMemberTeam(target,team,target);
                memberTeamRepository.save(mt);
                try {
                    groupService.acceptMember(target.getId(),team.getId());
                }catch (Exception e){
                    log.error("Detected multiple access");
                    log.error("msg: {}",e.getMessage());
                }
                countDownLatch.countDown();
            });
        }
        countDownLatch.await();
        log.info("logic is finished");
        List<MemberTeam> byTeam = memberTeamRepository.findByTeam(team);
        int size = byTeam.stream().filter(mt -> mt.isBelong()).collect(Collectors.toList()).size();
        log.info("now team size:"+size);
    }
    private Team makeTeam(Member owner){
        Movie movie = new Movie();
        movie.setMovieGenre(MovieType.ACTION);
        movie.setCategoryTeamList(new ArrayList<>());
        categoryRepository.save(movie);
        Team team = Team.builder().name("testMulTeam").maxMember(3L).owner(owner).category(movie).build();
        teamRepository.save(team);
        return team;
    }
    private Member makeMember(){
        UUID uuid = UUID.randomUUID();
        Member build = Member.init().email(uuid.toString()).username(uuid.toString()).password("123").build();
        build.setFcmToken("djDBQUAQTcy-Q7yn5Y_uZG:APA91bHrdu3OC_7EK_XS-UnepZs7H0Z29wcphC2JRqrwG-XHWNOwAIPXJoSOUpGc9RbeCDshPryJJXG8Mry_YA2WyXYh06epNUaJkGBeLQcHXK9wU7pEfhtdTuEnaVAL6cSJ60p5Y29v");
        memberRepository.save(build);
        return build;
    }
}
