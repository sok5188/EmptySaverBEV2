package com.example.emptySaver;

import com.example.emptySaver.domain.entity.category.*;
//import com.example.emptySaver.repository.CategoryRepository;
import com.example.emptySaver.repository.CategoryRepository;
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
        //데이터가 있으면 false -> 없으면 TRue
        public boolean checkUpdate(){
            return !(categoryRepository.count()>0);
        }
        public void intiCategoryInfo(){
            saveGame();
            saveMovie();
            saveSports();
            saveStudy();
        }

        private void saveGame() {
            List<Game> gameList = new ArrayList<>();
            Arrays.stream(GameType.values()).forEach(t->{
                Game game=new Game();
                game.setGameGenre(t);
                gameList.add(game);
            });
            categoryRepository.saveAll(gameList);
        }
        private void saveMovie() {
            List<Movie> movieList = new ArrayList<>();
            Arrays.stream(MovieType.values()).forEach(t->{
                Movie movie=new Movie();
                movie.setMovieGenre(t);
                movieList.add(movie);
            });
            categoryRepository.saveAll(movieList);
        }
        private void saveSports() {
            List<Sports> sportsList = new ArrayList<>();
            Arrays.stream(SportsType.values()).forEach(t->{
                Sports sports = new Sports();
                sports.setSportType(t);
                sportsList.add(sports);
            });
            categoryRepository.saveAll(sportsList);
        }
        private void saveStudy() {
            List<Study> studyList = new ArrayList<>();
            Arrays.stream(StudyType.values()).forEach(t->{
                Study study=new Study();
                study.setStudyType(t);
                studyList.add(study);
            });
            categoryRepository.saveAll(studyList);
        }
    }

}
