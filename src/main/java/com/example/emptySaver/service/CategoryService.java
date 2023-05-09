package com.example.emptySaver.service;

import com.example.emptySaver.domain.dto.CategoryDto;
import com.example.emptySaver.domain.entity.category.*;
import com.example.emptySaver.errorHandler.BaseException;
import com.example.emptySaver.errorHandler.BaseResponseStatus;
import com.example.emptySaver.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private static CategoryDto.res allCategory;
    private static final Map<String,String> labelMap=new HashMap<>();
    @PostConstruct
    public void setAllCategories() {
            List<CategoryDto.categoryType> typeList=new ArrayList<>();
            typeList.add(new CategoryDto.categoryType<>("game",
                    Arrays.stream(GameType.values()).map(t-> t.getLabel())
                        .collect(Collectors.toList())
            ));
            labelMap.putAll(Arrays.stream(GameType.values()).collect(Collectors.toMap(GameType::getLabel,GameType::getKey)));

            typeList.add(new CategoryDto.categoryType<>("movie",
                    Arrays.stream(MovieType.values()).map(t-> t.getLabel())
                            .collect(Collectors.toList())
                    ));
            labelMap.putAll(Arrays.stream(MovieType.values()).collect(Collectors.toMap(MovieType::getLabel,MovieType::getKey)));

            typeList.add(new CategoryDto.categoryType<>("sports",
                    Arrays.stream(SportsType.values()).map(t-> t.getLabel())
                            .collect(Collectors.toList())
            ));
            labelMap.putAll(Arrays.stream(SportsType.values()).collect(Collectors.toMap(SportsType::getLabel,SportsType::getKey)));

            typeList.add(new CategoryDto.categoryType<>("study",
                    Arrays.stream(StudyType.values()).map(t-> t.getLabel())
                            .collect(Collectors.toList())
                    ));
            labelMap.putAll(Arrays.stream(StudyType.values()).collect(Collectors.toMap(StudyType::getLabel,StudyType::getKey)));

            allCategory=new CategoryDto.res(typeList);
    }

    public CategoryDto.res getAllCategories() {
        return allCategory;
    }
    public Category getCategoryByLabel(String label){
        if(!labelMap.containsKey(label))
            throw new BaseException(BaseResponseStatus.INVALID_REQUEST);

        List<Game> allGame = categoryRepository.findAllGame();
        Optional<Game> gameOptional = allGame.stream().filter(g -> g.getGameGenre().getLabel().equals(label)).findAny();
        if(gameOptional.isPresent())
            return gameOptional.get();

        List<Sports> allSports = categoryRepository.findAllSports();
        Optional<Sports> sportsOptional = allSports.stream().filter(g -> g.getSportType().getLabel().equals(label)).findAny();
        if(sportsOptional.isPresent())
            return sportsOptional.get();

        List<Movie> allMovie = categoryRepository.findAllMovie();
        Optional<Movie> movieOptional = allMovie.stream().filter(m -> m.getMovieGenre().getLabel().equals(label)).findAny();
        if(movieOptional.isPresent())
            return movieOptional.get();

        List<Study> allStudy = categoryRepository.findAllStudy();
        Optional<Study> studyOptional = allStudy.stream().filter(m -> m.getStudyType().getLabel().equals(label)).findAny();
        if(studyOptional.isPresent())
            return movieOptional.get();

        throw new BaseException(BaseResponseStatus.INVALID_LABEL_NAME);
    }

    public String getLabelByCategory(Category category){
        log.info("now get label by category, id and class : "+category.getId()+ " / "+ category.getClass());
        if(category instanceof Game){
            Game game = (Game) category;
            return game.getGameGenre().getLabel();
        }else if(category instanceof Movie){
            Movie movie = (Movie) category;
            return movie.getMovieGenre().getLabel();
        }else if(category instanceof Sports){
            Sports sports= (Sports) category;
            return sports.getSportType().getLabel();
        }else if(category instanceof Study){
            Study study= (Study) category;
            return study.getStudyType().getLabel();
        }else{
            throw new BaseException(BaseResponseStatus.INVALID_CATEGORY_ID);
        }
    }
    public List<Category> getCategoryByName(String name){

        if(name.equals("game")){
            return categoryRepository.findAllGame();
        }else if(name.equals("movie"))
            return categoryRepository.findAllMovie();
        else if(name.equals("sports"))
            return categoryRepository.findAllSports();
        else if(name.equals("study"))
            return categoryRepository.findAllStudy();
        else throw new BaseException(BaseResponseStatus.INVALID_REQUEST);
    }

}
