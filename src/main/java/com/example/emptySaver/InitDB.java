package com.example.emptySaver;

import com.example.emptySaver.domain.entity.category.*;
//import com.example.emptySaver.repository.CategoryRepository;
import com.example.emptySaver.repository.CategoryRepository;
import com.example.emptySaver.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class InitDB {
    private final InitCategory initCategory;
    //TODO: 프로덕트 상태면 아예 지워버리던가 합시다 뭐 업데이트할꺼면.. 추가해도되는데.. 근데.. 연관관계때문에 다 지웠다가 넣는건 안되니
    // 프로덕션에서 추가되는 기능은 직접 DB에 추가하는 형식 .. !
    @PostConstruct
    public void init(){
        if(initCategory.checkUpdate())
            initCategory.intiCategoryInfo();
        else log.info("Category Data Already Exist!");
    }

    @Service
    @Transactional
    @RequiredArgsConstructor
    static class InitCategory {
        private final CategoryRepository categoryRepository;
        private final CategoryService categoryService;
        //데이터가 있으면 false -> 없으면 TRue
        public boolean checkUpdate(){
            if(categoryRepository.count()<categoryService.getTotalLabelCount())
                return true;
            else return false;
        }
        public void intiCategoryInfo(){
            //TODO: 추가는 가능하지만 삭제,변경이 불가능함 (DB에서 직접 수정,,.? 삭제는 일단 안됨 (Team FK))
            savePlay();
            saveMovie();
            saveSports();
            saveStudy();
            saveFree();
        }

        private void savePlay() {
            List<Play> allPlay = categoryService.findAllPlay();
            if(allPlay.size()==categoryService.getTargetCategoryLabelCount("play")){
                return;
            }
            List<Play> playList = new ArrayList<>();
            Arrays.stream(PlayType.values()).forEach(t->{
                if(!allPlay.stream().filter(g->g.getPlayType().equals(t)).findAny().isPresent()){
                    Play play =new Play();
                    play.setPlayType(t);
                    playList.add(play);
                }
            });
            categoryRepository.saveAll(playList);
        }
        private void saveMovie() {
            List<Movie> allMovie = categoryService.findAllMovie();
            if(allMovie.size()==categoryService.getTargetCategoryLabelCount("movie")){
                return;
            }
            List<Movie> movieList = new ArrayList<>();
            Arrays.stream(MovieType.values()).forEach(t->{
                //등록된 라벨 (enum기준)이 DB에 없다면 새로 만들어서 추가
                if(!allMovie.stream().filter(m->m.getMovieGenre().equals(t)).findAny().isPresent()){
                    Movie movie=new Movie();
                    movie.setMovieGenre(t);
                    movieList.add(movie);
                }

            });
            categoryRepository.saveAll(movieList);
        }
        private void saveSports() {
            List<Sports> allSports = categoryService.findAllSports();
            if(allSports.size()==categoryService.getTargetCategoryLabelCount("sports"))
                return;
            List<Sports> sportsList = new ArrayList<>();
            Arrays.stream(SportsType.values()).forEach(t->{
                if(!allSports.stream().filter(s->s.getSportType().equals(t)).findAny().isPresent()){
                    Sports sports = new Sports();
                    sports.setSportType(t);
                    sportsList.add(sports);
                }
            });
            categoryRepository.saveAll(sportsList);
        }
        private void saveStudy() {
            List<Study> allStudy = categoryService.findAllStudy();
            if(allStudy.size()==categoryService.getTargetCategoryLabelCount("study"))
                return;
            List<Study> studyList = new ArrayList<>();
            Arrays.stream(StudyType.values()).forEach(t->{
                if(!allStudy.stream().filter(s->s.getStudyType().equals(t)).findAny().isPresent()){
                    Study study=new Study();
                    study.setStudyType(t);
                    studyList.add(study);
                }
            });
            categoryRepository.saveAll(studyList);
        }
        private void saveFree(){
            if(categoryService.findAllFree().size()==categoryService.getTargetCategoryLabelCount("free"))
                return;
            //얘는 어차피 자율라벨 하나니깐 지나갑니다.
            List<Free> freeList = new ArrayList<>();
            Arrays.stream(FreeType.values()).forEach(t->{
                Free free =new Free();
                free.setFreeType(t);
                freeList.add(free);
            });
            categoryRepository.saveAll(freeList);
        }
    }

}
