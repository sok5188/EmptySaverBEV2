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
    private static final Map<String,List<String>> categoryMap=new HashMap<>();
    @PostConstruct
    public void setAllCategories() {
        List<CategoryDto.categoryType> typeList=new ArrayList<>();
        List<String> gameCollect = Arrays.stream(GameType.values()).map(t -> t.getLabel()).collect(Collectors.toList());
        categoryMap.put("game",gameCollect);
        typeList.add(new CategoryDto.categoryType<>("game", gameCollect));
        labelMap.putAll(Arrays.stream(GameType.values()).collect(Collectors.toMap(GameType::getLabel,GameType::getKey)));

        List<String> movieCollect = Arrays.stream(MovieType.values()).map(t -> t.getLabel()).collect(Collectors.toList());
        categoryMap.put("movie",movieCollect);
        typeList.add(new CategoryDto.categoryType<>("movie",movieCollect));
        labelMap.putAll(Arrays.stream(MovieType.values()).collect(Collectors.toMap(MovieType::getLabel,MovieType::getKey)));

        List<String> sportsCollect = Arrays.stream(SportsType.values()).map(t -> t.getLabel()).collect(Collectors.toList());
        categoryMap.put("sports",sportsCollect);
        typeList.add(new CategoryDto.categoryType<>("sports", sportsCollect));
        labelMap.putAll(Arrays.stream(SportsType.values()).collect(Collectors.toMap(SportsType::getLabel,SportsType::getKey)));

        List<String> studyCollect = Arrays.stream(StudyType.values()).map(t -> t.getLabel()).collect(Collectors.toList());
        categoryMap.put("study",studyCollect);
        typeList.add(new CategoryDto.categoryType<>("study", studyCollect));
        labelMap.putAll(Arrays.stream(StudyType.values()).collect(Collectors.toMap(StudyType::getLabel,StudyType::getKey)));

        List<String> etcCollect=new ArrayList<>();
        etcCollect.add("자율");
        categoryMap.put("etc",etcCollect);
        typeList.add(new CategoryDto.categoryType<>("etc",etcCollect));
        labelMap.putAll(Arrays.stream(EtcType.values()).collect(Collectors.toMap(EtcType::getLabel,EtcType::getKey)));

        allCategory=new CategoryDto.res(typeList);
    }
    public Integer getLabelCount(){
        return labelMap.size();
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
            return studyOptional.get();

        List<Etc> allEtc = categoryRepository.findAllEtc();
        Optional<Etc> etcOptional = allEtc.stream().filter(e -> e.getEtcType().getLabel().equals(label)).findAny();
        if(etcOptional.isPresent())
            return etcOptional.get();

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
        }else if(category instanceof Etc){
            Etc etc=(Etc) category;
            return etc.getEtcType().getLabel();
        }
        else{
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
        else if(name.equals("etc"))
            return categoryRepository.findAllEtc();
        else throw new BaseException(BaseResponseStatus.INVALID_REQUEST);
    }
    public List<CategoryDto.categoryInfo> getCategoryNames(){
        List<CategoryDto.categoryInfo> result = new ArrayList<>();
        result.add(new CategoryDto.categoryInfo("game","게임"));
        result.add(new CategoryDto.categoryInfo("movie","영화"));
        result.add(new CategoryDto.categoryInfo("sports","스포츠"));
        result.add(new CategoryDto.categoryInfo("study","스터디"));
        result.add(new CategoryDto.categoryInfo("etc","자율"));
        return result;
    }
    public CategoryDto.categoryType getLabelInfoByCategoryName(String type){
        if(!categoryMap.containsKey(type))
            throw new BaseException(BaseResponseStatus.INVALID_CATEGORY_ID);
        List<String> labelList = categoryMap.get(type);
        return new CategoryDto.categoryType(type,labelList);
    }

}
